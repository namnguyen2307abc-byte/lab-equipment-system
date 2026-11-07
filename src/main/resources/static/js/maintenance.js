(() => {
    'use strict';
    document.querySelectorAll('[data-character-count]').forEach(input => {
        const output = input.closest('.form-field').querySelector('[data-character-output]');
        if (!output) return;
        const update = () => { output.textContent = `${input.value.length} / ${input.maxLength}`; };
        input.addEventListener('input', update);
        update();
    });
    document.querySelectorAll('[data-completion-form]').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.elements.outcome.value === 'UNREPAIRABLE'
                    && !window.confirm('Xác nhận thiết bị không sửa được và chuyển sang ngừng sử dụng?')) {
                event.preventDefault();
            }
        });
    });
})();
