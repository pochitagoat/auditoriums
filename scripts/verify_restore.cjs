const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');

async function snapshot(base) {
    async function json(url) {
        const response = await fetch(base + url);
        assert.equal(response.status, 200, url);
        return response.json();
    }
    const result = { departments: await json('/api/departments'), equipmentTypes: await json('/api/additional-equipment'), rooms: [] };
    let page = 0;
    let totalPages;
    do {
        const list = await json('/api/auditoriums?size=100&page=' + page);
        totalPages = list.totalPages;
        for (const entry of list.content) {
            const room = { card: await json('/api/auditoriums/' + entry.id) };
            for (const resource of ['workstations', 'projectors', 'printers']) {
                room[resource] = await json('/api/auditoriums/' + entry.id + '/' + resource + '?includeWrittenOff=true');
            }
            room.photos = await json('/api/auditoriums/' + entry.id + '/photos');
            for (const photo of room.photos) {
                const response = await fetch(base + photo.url);
                assert.equal(response.status, 200, photo.url);
                photo.sha256 = crypto.createHash('sha256').update(Buffer.from(await response.arrayBuffer())).digest('hex');
            }
            result.rooms.push(room);
        }
        page++;
    } while (page < totalPages);
    return result;
}

(async () => {
    const source = process.env.AUDITORIUMS_SOURCE_URL;
    const restored = process.env.AUDITORIUMS_RESTORED_URL;
    if (!source || !restored) throw new Error('Задайте AUDITORIUMS_SOURCE_URL и AUDITORIUMS_RESTORED_URL');
    const before = await snapshot(source);
    const after = await snapshot(restored);
    assert.deepEqual(after, before, 'Восстановленные данные или содержимое фотографий отличаются');
    const report = { rooms: before.rooms.length, departments: before.departments.length,
        equipmentTypes: before.equipmentTypes.length, photos: before.rooms.reduce((sum, room) => sum + room.photos.length, 0),
        comparison: 'all API fields and all photo SHA-256 hashes match' };
    const directory = path.resolve('target', 'verification');
    fs.mkdirSync(directory, { recursive: true });
    fs.writeFileSync(path.join(directory, 'restore-report.json'), JSON.stringify(report, null, 2));
    console.log(JSON.stringify(report));
})().catch(error => { console.error(error); process.exitCode = 1; });
