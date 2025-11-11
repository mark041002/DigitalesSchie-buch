/**
 * Layout Interaktionen - Hover-Effekte und Debouncing
 *
 * Dieses Modul kümmert sich um:
 * - Details-Hover-Effekte
 * - Navigation Debouncing
 */

// Details Hover-Effekte
document.addEventListener('DOMContentLoaded', () => {
    initDetailsHoverEffects();
    initNavigationDebouncing();
});

/**
 * Initialisiert Hover-Effekte für Details-Elemente
 */
function initDetailsHoverEffects() {
    const observer = new MutationObserver(() => {
        const detailsElements = document.querySelectorAll('vaadin-details[data-interactive="true"]');

        detailsElements.forEach(details => {
            if (details.dataset.hoverInitialized) return;

            details.addEventListener('mouseenter', () => {
                details.style.boxShadow = 'var(--lumo-box-shadow-s)';
                details.style.background = 'var(--lumo-contrast-10pct)';
            });

            details.addEventListener('mouseleave', () => {
                details.style.boxShadow = 'var(--lumo-box-shadow-xs)';
                details.style.background = 'var(--lumo-contrast-5pct)';
            });

            details.dataset.hoverInitialized = 'true';
        });
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}

/**
 * Initialisiert Debouncing für Navigations-Items
 */
function initNavigationDebouncing() {
    const observer = new MutationObserver(() => {
        const debouncedItems = document.querySelectorAll('.debounced-nav-item');

        debouncedItems.forEach(item => {
            if (item.dataset.debounceInitialized) return;

            let lastClick = 0;

            item.addEventListener('click', function(e) {
                const now = Date.now();
                if (now - lastClick < 500) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }
                lastClick = now;
            }, true);

            item.dataset.debounceInitialized = 'true';
        });
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}