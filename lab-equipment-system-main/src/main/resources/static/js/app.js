(() => {
    'use strict';

    const sidebar = document.getElementById('app-sidebar');
    const toggle = document.querySelector('.sidebar-toggle');
    const closeButton = document.querySelector('.sidebar-close');
    const overlay = document.querySelector('.sidebar-overlay');
    const mobile = window.matchMedia('(max-width: 767px)');

    if (!sidebar || !toggle || !closeButton || !overlay) return;

    function setMenuOpen(open, restoreFocus = true) {
        const expanded = mobile.matches && open;
        document.body.classList.toggle('sidebar-open', expanded);
        toggle.setAttribute('aria-expanded', String(expanded));
        overlay.hidden = !expanded;
        sidebar.inert = mobile.matches && !expanded;
        if (expanded) {
            requestAnimationFrame(() => requestAnimationFrame(() => {
                if (document.body.classList.contains('sidebar-open')) closeButton.focus();
            }));
        }
        else if (restoreFocus && mobile.matches) toggle.focus();
    }

    toggle.addEventListener('click', () => setMenuOpen(toggle.getAttribute('aria-expanded') !== 'true'));
    closeButton.addEventListener('click', () => setMenuOpen(false));
    overlay.addEventListener('click', () => setMenuOpen(false));
    mobile.addEventListener('change', () => setMenuOpen(false, false));

    document.addEventListener('keydown', event => {
        if (!document.body.classList.contains('sidebar-open')) return;
        if (event.key === 'Escape') {
            event.preventDefault();
            setMenuOpen(false);
        }
        if (event.key === 'Tab') {
            const elements = [...sidebar.querySelectorAll('a[href], button:not([disabled])')]
                .filter(element => element.getClientRects().length > 0);
            const first = elements[0];
            const last = elements[elements.length - 1];
            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        }
    });

    setMenuOpen(false, false);
})();
