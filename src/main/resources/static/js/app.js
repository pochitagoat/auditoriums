'use strict';

function showErrors(form, message, fields = {}) {
    const general = form.querySelector('.form-error');
    general.textContent = message;
    general.hidden = false;
    general.focus();
    for (const [field, error] of Object.entries(fields)) {
        const slot = form.querySelector(`[data-field-error="${CSS.escape(field)}"]`);
        if (slot) slot.textContent = error;
        const control = form.elements.namedItem(field);
        if (control instanceof HTMLElement) control.setAttribute('aria-invalid', 'true');
    }
}

function payload(form) {
    const result = {};
    for (const control of form.elements) {
        if (!control.name || control.disabled || control.type === 'submit') continue;
        const value = control.value.trim();
        if (control.dataset.type === 'boolean') result[control.name] = value === '' ? null : value === 'true';
        else if (control.type === 'number' || control.dataset.type === 'number') result[control.name] = value === '' ? null : Number(value);
        else result[control.name] = value === '' ? null : value;
    }
    return result;
}

for (const form of document.querySelectorAll('[data-api-form]')) {
    for (const button of form.querySelectorAll('button[type="submit"]')) button.disabled = false;
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (form.dataset.sending === 'true' || !form.reportValidity()) return;
        if (form.dataset.confirm && !window.confirm(form.dataset.confirm)) return;
        form.dataset.sending = 'true';
        const buttons = Array.from(form.querySelectorAll('button[type="submit"]'));
        buttons.forEach(button => { button.disabled = true; });
        form.querySelectorAll('[data-field-error]').forEach(slot => { slot.textContent = ''; });
        form.querySelectorAll('[aria-invalid]').forEach(control => control.removeAttribute('aria-invalid'));
        form.querySelector('.form-error').hidden = true;
        try {
            const method = form.dataset.method || 'POST';
            let url = form.dataset.api;
            const options = {method, headers: {'Accept': 'application/json'}};
            if (method === 'DELETE') {
                url += '?' + new URLSearchParams(payload(form));
            } else if (form.hasAttribute('data-multipart')) {
                options.body = new FormData(form);
            } else {
                options.headers['Content-Type'] = 'application/json';
                options.body = JSON.stringify(payload(form));
            }
            const response = await fetch(url, options);
            const body = response.status === 204 ? null : await response.json().catch(() => null);
            if (!response.ok) {
                showErrors(form, body?.message || 'Не удалось выполнить операцию. Повторите попытку.', body?.fieldErrors);
                return;
            }
            let next;
            if (form.dataset.kind === 'auditorium') {
                next = new URL('/auditoriums/' + body.id, location.origin);
                next.searchParams.set('returnTo', form.dataset.returnTo);
            } else next = new URL(form.dataset.success || location.href, location.origin);
            next.searchParams.set('saved', '1');
            location.assign(next.href);
        } catch (error) {
            showErrors(form, 'Не удалось связаться с сервером. Введенные данные сохранены в форме. Повторите попытку.');
        } finally {
            form.dataset.sending = 'false';
            buttons.forEach(button => { button.disabled = false; });
        }
    });
}

const searchForm = document.querySelector('.search-panel form');
if (searchForm) {
    const filters = searchForm.querySelector('.filter-details');
    if (Array.from(filters.querySelectorAll('input, select')).some(control => control.value !== '')) filters.open = true;
    searchForm.querySelector('[name="size"]').addEventListener('change', () => searchForm.requestSubmit());
    // Пустые значения не загромождают URL; номер страницы при поиске отсутствует и равен нулю.
    searchForm.addEventListener('formdata', event => {
        for (const [key, value] of Array.from(event.formData.entries())) {
            if (typeof value === 'string' && !value.trim()) event.formData.delete(key);
        }
    });
}
