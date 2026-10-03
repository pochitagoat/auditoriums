/* Проверяет только явно указанное disposable окружение. Playwright — инструмент
   проверок; приложение не имеет npm-зависимостей и frontend-сборки. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');

(async () => {
    const baseURL = process.env.AUDITORIUMS_BROWSER_TEST_URL;
    if (!baseURL) throw new Error('Укажите AUDITORIUMS_BROWSER_TEST_URL отдельного тестового окружения');
    const browser = await chromium.launch({ headless: true, channel: process.env.PLAYWRIGHT_CHANNEL || 'msedge', args: ['--no-proxy-server'] });
    const context = await browser.newContext({ baseURL, viewport: { width: 1440, height: 1000 } });
    const page = await context.newPage();
    const faults = [];
    page.on('pageerror', error => faults.push(error.message));
    let cancelNext = false;
    page.on('dialog', dialog => { if (cancelNext) { cancelNext = false; return dialog.dismiss(); } return dialog.accept(); });
    let mutations = 0;
    page.on('request', request => { if (request.url().includes('/api/') && request.method() !== 'GET') mutations++; });
    const stamp = Date.now().toString();
    const author = 'Проверяющий сотрудник';
    const directory = path.resolve('target', 'verification');
    fs.mkdirSync(directory, { recursive: true });
    const step = name => process.stdout.write(name + '\n');
    async function submit(form) {
        await Promise.all([page.waitForNavigation({ waitUntil: 'domcontentloaded' }), form.locator('button[type=submit]').click()]);
    }
    async function api(url) {
        const response = await context.request.get(url);
        assert.equal(response.status(), 200);
        return response.json();
    }
    try {
        for (const [base, name] of [['departments', 'Кафедра проверки ' + stamp], ['additional-equipment', 'Колонки проверки ' + stamp]]) {
            await page.goto('/' + base + '/new');
            await page.locator('[name=name]').fill(name);
            await page.locator('[name=changedBy]').fill(author);
            await submit(page.locator('form[data-method=POST]'));
        }
        const department = (await api('/api/departments')).find(item => item.name.endsWith(stamp));
        const type = (await api('/api/additional-equipment')).find(item => item.name.endsWith(stamp));
        assert.ok(department && type);
        step('Справочники созданы через интерфейс');

        await page.goto('/auditoriums/new?returnTo=' + encodeURIComponent('/auditoriums?q=Обход&size=50&page=0'));
        await page.locator('[name=name]').fill('Обход ' + stamp);
        await page.locator('[name=type]').selectOption('COMPUTER_CLASSROOM');
        await page.locator('[name=departmentId]').selectOption(String(department.id));
        await page.locator('[name=building]').fill('Проверка');
        await page.locator('[name=floor]').fill('-1');
        await page.locator('[name=capacityStudents]').fill('0');
        await page.locator('[name=capacityStudentsExtra]').fill('5');
        await page.locator('[name=boardType]').selectOption('MARKER');
        await page.locator('[name=changedBy]').fill(author);
        const beforeCancel = mutations;
        cancelNext = true;
        await page.locator('button[type=submit]').click();
        assert.equal(mutations, beforeCancel);
        assert.equal(await page.locator('[name=name]').inputValue(), 'Обход ' + stamp);
        await submit(page.locator('form[data-api-form]'));
        const roomId = Number(new URL(page.url()).pathname.split('/').pop());
        assert.ok(roomId);
        assert.match(await page.locator('.back-link').getAttribute('href'), /q=Обход|q=%D0/);
        step('Создание, отмена подтверждения и контекст списка проверены');

        const roomUrl = new URL(page.url()).pathname + new URL(page.url()).search;
        await page.getByRole('link', { name: 'Редактировать аудиторию', exact: true }).click();
        const second = await context.newPage();
        second.on('dialog', dialog => dialog.accept());
        await second.goto(page.url());
        await page.locator('[name=description]').fill('Первый клиент');
        await page.locator('[name=changedBy]').fill(author);
        await submit(page.locator('form[data-api-form]'));
        await second.locator('[name=description]').fill('Второй клиент — ввод сохранен');
        await second.locator('[name=changedBy]').fill('Другой сотрудник');
        await second.locator('button[type=submit]').click();
        await second.locator('.form-error:not([hidden])').waitFor();
        assert.equal(await second.locator('[name=description]').inputValue(), 'Второй клиент — ввод сохранен');
        assert.equal((await api('/api/auditoriums/' + roomId)).description, 'Первый клиент');
        await second.close();
        step('Конфликт двух клиентов сохраняет введенные значения');

        for (const resource of ['workstations', 'projectors', 'printers']) {
            const section = page.locator('#' + resource);
            await section.locator('details.add-equipment > summary').click();
            const form = section.locator('details.add-equipment form');
            if (resource === 'workstations') await form.locator('[name=pcName]').fill('UI-' + stamp);
            else await form.locator('[name=model]').fill('Модель проверки');
            await form.locator('[name=changedBy]').fill(author);
            await submit(form);
            assert.equal((await api('/api/auditoriums/' + roomId + '/' + resource)).length, 1);
        }
        const workstationSection = page.locator('#workstations');
        await workstationSection.locator('details.add-equipment > summary').click();
        const duplicateForm = workstationSection.locator('details.add-equipment form');
        await duplicateForm.locator('[name=pcName]').fill('UI-' + stamp);
        await duplicateForm.locator('[name=changedBy]').fill(author);
        await duplicateForm.locator('button[type=submit]').click();
        await duplicateForm.locator('.form-error:not([hidden])').waitFor();
        assert.ok(await duplicateForm.locator('[data-field-error=pcName]').textContent());
        assert.equal(await duplicateForm.locator('[name=pcName]').inputValue(), 'UI-' + stamp);
        await page.goto(roomUrl);

        for (const resource of ['workstations', 'projectors', 'printers']) {
            let row = page.locator('#' + resource + ' article');
            await row.locator('summary').filter({ hasText: /^Редактировать$/ }).click();
            let form = row.locator('form[data-method=PUT]');
            await form.locator(resource === 'workstations' ? '[name=locationInfo]' : '[name=warehouse]').fill('Обновлено');
            await form.locator('[name=changedBy]').fill(author);
            await submit(form);
            row = page.locator('#' + resource + ' article');
            await row.locator('summary').filter({ hasText: /^Списать$/ }).click();
            form = row.locator('form[data-method=PATCH]');
            await form.locator('[name=changedBy]').fill(author);
            await submit(form);
            assert.equal((await api('/api/auditoriums/' + roomId + '/' + resource)).length, 0);
            if (await page.getByRole('link', { name: 'Показать списанное', exact: true }).count()) {
                await page.getByRole('link', { name: 'Показать списанное', exact: true }).click();
            }
            row = page.locator('#' + resource + ' article');
            await row.locator('summary').filter({ hasText: /^Восстановить$/ }).click();
            form = row.locator('form[data-method=PATCH]');
            await form.locator('[name=changedBy]').fill(author);
            await submit(form);
            assert.equal((await api('/api/auditoriums/' + roomId + '/' + resource)).length, 1);
        }
        step('Создание, редактирование, списание и восстановление всех трех видов оборудования проверены');

        await page.locator('#additional details.add-equipment > summary').click();
        const linkForm = page.locator('#additional form[data-method=POST]');
        await linkForm.locator('[name=additionalEquipmentId]').selectOption(String(type.id));
        await linkForm.locator('[name=changedBy]').fill(author);
        await submit(linkForm);
        assert.ok((await api('/api/auditoriums/' + roomId + '/additional-equipment')).some(item => item.id === type.id));
        await page.locator('#additional .linked-type summary').click();
        const unlinkForm = page.locator('#additional form[data-method=DELETE]');
        await unlinkForm.locator('[name=changedBy]').fill(author);
        await submit(unlinkForm);
        assert.ok((await api('/api/additional-equipment')).some(item => item.id === type.id));

        const imageBytes = await page.screenshot();
        let photoForm = page.locator('form[data-multipart]');
        await photoForm.locator('[name=file]').setInputFiles({ name: 'unsupported.webp', mimeType: 'image/webp', buffer: imageBytes });
        await photoForm.locator('[name=changedBy]').fill(author);
        await photoForm.locator('button[type=submit]').click();
        await photoForm.locator('.form-error:not([hidden])').waitFor();
        assert.ok(await photoForm.locator('[data-field-error=file]').textContent());
        await photoForm.locator('[name=file]').setInputFiles({ name: 'аудитория.png', mimeType: 'image/png', buffer: imageBytes });
        await submit(photoForm);
        let images = await api('/api/auditoriums/' + roomId + '/photos');
        assert.equal(images.length, 1);
        const imageResponse = await context.request.get(images[0].url);
        assert.equal(crypto.createHash('sha256').update(await imageResponse.body()).digest('hex'), crypto.createHash('sha256').update(imageBytes).digest('hex'));
        await page.locator('#photos figure summary').click();
        let removePhoto = page.locator('#photos form[data-method=DELETE]');
        await removePhoto.locator('[name=changedBy]').fill(author);
        await submit(removePhoto);
        assert.equal((await api('/api/auditoriums/' + roomId + '/photos')).length, 0);
        photoForm = page.locator('form[data-multipart]');
        await photoForm.locator('[name=file]').setInputFiles({ name: 'аудитория.png', mimeType: 'image/png', buffer: imageBytes });
        await photoForm.locator('[name=changedBy]').fill(author);
        await submit(photoForm);
        images = await api('/api/auditoriums/' + roomId + '/photos');
        step('Типы оборудования и загрузка/просмотр/удаление фотографий проверены');

        await page.goto('/auditoriums?q=' + encodeURIComponent('Обход ' + stamp) + '&size=50');
        await page.locator('.filter-details > summary').click();
        await page.locator('[name=building]').fill('Проверка');
        await page.locator('[name=type]').selectOption('COMPUTER_CLASSROOM');
        await page.locator('[name=hasWorkstations]').selectOption('true');
        await page.locator('[name=boardType]').selectOption('MARKER');
        await Promise.all([page.waitForNavigation(), page.getByRole('button', { name: 'Найти', exact: true }).click()]);
        assert.equal(await page.locator('tbody tr').count(), 1);
        await page.locator('.room-link').click();
        await page.locator('.back-link').click();
        assert.equal(new URL(page.url()).searchParams.get('building'), 'Проверка');
        assert.equal(await page.locator('[name=hasWorkstations]').inputValue(), 'true');
        step('Поиск, комбинированные фильтры и возврат к списку проверены');

        const temporaryIds = [];
        for (let index = 0; index < 21; index++) {
            const response = await context.request.post('/api/auditoriums', { data: {
                name: 'Страница ' + stamp + ' ' + index, type: 'CLASSROOM', changedBy: author } });
            assert.equal(response.status(), 201);
            temporaryIds.push((await response.json()).id);
        }
        await page.goto('/auditoriums?q=' + encodeURIComponent('Страница ' + stamp) + '&size=20');
        assert.equal(await page.locator('tbody tr').count(), 20);
        await page.getByRole('link', { name: 'Далее →', exact: true }).click();
        assert.equal(await page.locator('tbody tr').count(), 1);
        await page.locator('.room-link').click();
        await page.locator('.back-link').click();
        assert.equal(new URL(page.url()).searchParams.get('page'), '1');
        assert.equal(await page.locator('tbody tr').count(), 1);
        await page.locator('[name=size]').selectOption('50');
        await page.waitForURL(url => url.searchParams.get('size') === '50' && !url.searchParams.has('page'));
        assert.equal(await page.locator('tbody tr').count(), 21);
        for (const id of temporaryIds) {
            const response = await context.request.delete('/api/auditoriums/' + id, { params: { changedBy: author, version: '0' } });
            assert.equal(response.status(), 204);
        }
        await page.goto('/auditoriums/new');
        await page.locator('[name=name]').fill('Удаление проверки ' + stamp);
        await page.locator('[name=type]').selectOption('CLASSROOM');
        await page.locator('[name=changedBy]').fill(author);
        await submit(page.locator('form[data-api-form]'));
        const deletedRoom = Number(new URL(page.url()).pathname.split('/').pop());
        await page.locator('main > details.danger-zone > summary').click();
        const deletion = page.locator('main > details.danger-zone form');
        await deletion.locator('[name=changedBy]').fill(author);
        await submit(deletion);
        assert.equal((await context.request.get('/api/auditoriums/' + deletedRoom)).status(), 404);
        step('Несколько страниц, смена размера и удаление пустой аудитории проверены');

        await page.goto('/auditoriums');
        await page.screenshot({ path: path.join(directory, 'catalog-desktop.png'), fullPage: true });
        await page.setViewportSize({ width: 390, height: 844 });
        for (const [url, name] of [['/auditoriums', 'catalog-mobile'], [roomUrl, 'card-mobile'], ['/auditoriums/' + roomId + '/edit', 'form-mobile'], ['/departments', 'departments-mobile']]) {
            await page.goto(url);
            assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), 'Страница не должна выходить за ширину экрана: ' + url);
            await page.screenshot({ path: path.join(directory, name + '.png'), fullPage: true });
        }
        for (const url of ['/requests', '/statistics']) {
            await page.goto(url);
            assert.ok(await page.getByText('Раздел появится позже').isVisible());
        }
        const noJsContext = await browser.newContext({ baseURL, javaScriptEnabled: false });
        const noJs = await noJsContext.newPage();
        await noJs.goto('/auditoriums/new');
        await noJs.locator('noscript .notice').waitFor({ state: 'visible' });
        assert.match(await noJs.locator('noscript .notice').textContent(), /Для сохранения изменений включите JavaScript/);
        assert.ok(await noJs.locator('button[type=submit]').isDisabled());
        await noJsContext.close();
        assert.deepEqual(faults, []);
        const report = { roomId, departmentId: department.id, typeId: type.id, photoId: images[0].id,
            photoUrl: images[0].url, photoSha256: crypto.createHash('sha256').update(imageBytes).digest('hex'),
            title: 'Обход ' + stamp, mutations, assertions: 'passed' };
        fs.writeFileSync(path.join(directory, 'browser-report.json'), JSON.stringify(report, null, 2));
        step('Узкий экран, страницы-заглушки и режим без JavaScript проверены');
    } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
