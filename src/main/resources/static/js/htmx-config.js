// HTMX Configuration for MyFinances

document.addEventListener('DOMContentLoaded', function() {
    // Configure HTMX defaults
    if (typeof htmx !== 'undefined') {
        
        // Set default headers for CSRF protection
        htmx.config.requestClass = 'htmx-request';
        htmx.config.indicatorClass = 'htmx-indicator';
        
        // Add CSRF token to all requests
        document.addEventListener('htmx:configRequest', function(evt) {
            const csrfToken = document.querySelector('meta[name="_csrf"]');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
            
            if (csrfToken && csrfHeader) {
                evt.detail.headers[csrfHeader.getAttribute('content')] = csrfToken.getAttribute('content');
            }
        });

        // Show loading indicators
        document.addEventListener('htmx:beforeRequest', function(evt) {
            const target = evt.target;
            
            // Add loading class
            target.classList.add('htmx-loading');
            
            // Disable buttons during request
            if (target.tagName === 'BUTTON' || target.type === 'submit') {
                target.disabled = true;
                const originalText = target.textContent;
                target.setAttribute('data-original-text', originalText);
                target.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>A carregar...';
            }
            
            // Show global loading indicator
            const loadingIndicator = document.getElementById('htmx-loading');
            if (loadingIndicator) {
                loadingIndicator.style.display = 'block';
            }
        });

        // Hide loading indicators
        document.addEventListener('htmx:afterRequest', function(evt) {
            const target = evt.target;
            
            // Remove loading class
            target.classList.remove('htmx-loading');
            
            // Re-enable buttons
            if (target.tagName === 'BUTTON' || target.type === 'submit') {
                target.disabled = false;
                const originalText = target.getAttribute('data-original-text');
                if (originalText) {
                    target.textContent = originalText;
                    target.removeAttribute('data-original-text');
                }
            }
            
            // Hide global loading indicator
            const loadingIndicator = document.getElementById('htmx-loading');
            if (loadingIndicator) {
                loadingIndicator.style.display = 'none';
            }
        });

        // Handle successful responses
        document.addEventListener('htmx:afterSettle', function(evt) {
            // Reinitialize Bootstrap components in new content
            const newContent = evt.target;
            
            // Initialize tooltips
            const tooltips = newContent.querySelectorAll('[data-bs-toggle="tooltip"]');
            tooltips.forEach(tooltip => {
                new bootstrap.Tooltip(tooltip);
            });
            
            // Initialize popovers
            const popovers = newContent.querySelectorAll('[data-bs-toggle="popover"]');
            popovers.forEach(popover => {
                new bootstrap.Popover(popover);
            });
            
            // Reinitialize form validation
            const forms = newContent.querySelectorAll('.needs-validation');
            forms.forEach(form => {
                form.addEventListener('submit', function(event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                });
            });
        });

        // Handle errors
        document.addEventListener('htmx:responseError', function(evt) {
            console.error('HTMX Response Error:', evt.detail);
            
            // Show error message
            if (window.MyFinances) {
                window.MyFinances.showErrorToast('Erro na resposta do servidor. Código: ' + evt.detail.xhr.status);
            } else {
                alert('Erro na resposta do servidor. Tente novamente.');
            }
        });

        document.addEventListener('htmx:sendError', function(evt) {
            console.error('HTMX Send Error:', evt.detail);
            
            // Show error message
            if (window.MyFinances) {
                window.MyFinances.showErrorToast('Erro de conexão. Verifique sua internet.');
            } else {
                alert('Erro de conexão. Verifique sua internet e tente novamente.');
            }
        });

        // Handle timeout
        document.addEventListener('htmx:timeout', function(evt) {
            console.warn('HTMX Timeout:', evt.detail);
            
            if (window.MyFinances) {
                window.MyFinances.showErrorToast('Operação demorou muito tempo. Tente novamente.');
            } else {
                alert('A operação demorou muito tempo. Tente novamente.');
            }
        });

        // Custom HTMX extensions
        htmx.defineExtension('loading-states', {
            onEvent: function(name, evt) {
                if (name === 'htmx:beforeRequest') {
                    // Find and show loading states
                    const loadingElements = document.querySelectorAll('[hx-loading]');
                    loadingElements.forEach(el => {
                        el.style.display = 'block';
                    });
                }
                
                if (name === 'htmx:afterRequest') {
                    // Hide loading states
                    const loadingElements = document.querySelectorAll('[hx-loading]');
                    loadingElements.forEach(el => {
                        el.style.display = 'none';
                    });
                }
            }
        });

        // Auto-refresh functionality
        htmx.defineExtension('auto-refresh', {
            onEvent: function(name, evt) {
                if (name === 'htmx:afterSettle') {
                    const target = evt.target;
                    const refreshInterval = target.getAttribute('hx-refresh-interval');
                    
                    if (refreshInterval) {
                        const interval = parseInt(refreshInterval) * 1000;
                        setTimeout(() => {
                            if (document.contains(target)) {
                                htmx.trigger(target, 'refresh');
                            }
                        }, interval);
                    }
                }
            }
        });
        
        console.log('HTMX configuration loaded successfully');
    } else {
        console.warn('HTMX not found. Some dynamic features may not work.');
    }
});

// Helper functions for HTMX
window.htmxHelpers = {
    // Trigger a refresh of an element
    refresh: function(selector) {
        const elements = document.querySelectorAll(selector);
        elements.forEach(el => {
            htmx.trigger(el, 'refresh');
        });
    },
    
    // Show confirmation before action
    confirmAction: function(message, element) {
        if (confirm(message)) {
            htmx.trigger(element, 'confirmed');
            return true;
        }
        return false;
    },
    
    // Update URL without page reload
    updateUrl: function(url) {
        if (history.pushState) {
            history.pushState({}, '', url);
        }
    }
};