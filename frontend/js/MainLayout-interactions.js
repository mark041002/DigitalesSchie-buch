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

// Expose helper functions for server-side executeJs calls
// Adds hover effects to a specific details element (used by server-side code)
window.addSidebarHoverEffects = function(detailsEl) {
    try {
        if (!detailsEl) return;
        // If it's a Vaadin Element object proxy, try to get the DOM element
        const el = detailsEl instanceof Element ? detailsEl : (detailsEl && detailsEl.domElement) || detailsEl;
        if (!el) return;
        if (el.__sidebarHoverInit) return;

        el.addEventListener('mouseenter', () => {
            el.style.boxShadow = 'var(--lumo-box-shadow-s)';
            el.style.background = 'var(--lumo-contrast-10pct)';
        });

        el.addEventListener('mouseleave', () => {
            el.style.boxShadow = 'var(--lumo-box-shadow-xs)';
            el.style.background = 'var(--lumo-contrast-5pct)';
        });

        el.__sidebarHoverInit = true;
    } catch (e) {
        // swallow errors to avoid breaking FlowClient
        console.error('addSidebarHoverEffects error', e);
    }
};

// Adds a debounced click handler to a navigation item. timeoutMillis is optional.
window.addNavigationDebounce = function(itemEl, timeoutMillis) {
    try {
        if (!itemEl) return;
        const el = itemEl instanceof Element ? itemEl : (itemEl && itemEl.domElement) || itemEl;
        if (!el) return;
        if (el.__debounceInit) return;

        const timeout = typeof timeoutMillis === 'number' ? timeoutMillis : 500;
        let lastClick = 0;

        el.addEventListener('click', function(e) {
            const now = Date.now();
            if (now - lastClick < timeout) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
            lastClick = now;
        }, true);

        el.__debounceInit = true;
    } catch (e) {
        console.error('addNavigationDebounce error', e);
    }
};