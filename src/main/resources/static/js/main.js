// MyFinances - Main JavaScript

class MyFinances {
    constructor() {
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.initializeComponents();
        this.setupFormValidation();
        this.setupToasts();
    }

    setupEventListeners() {
        // Delete confirmation modal
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-bs-toggle="delete-modal"]')) {
                this.setupDeleteModal(e.target);
            }
        });

        // Form submission with loading state
        document.addEventListener('submit', (e) => {
            if (e.target.matches('.needs-loading')) {
                this.showLoading(e.target);
            }
        });

        // Auto-hide alerts
        document.addEventListener('DOMContentLoaded', () => {
            this.autoHideAlerts();
        });

        // Number formatting for currency inputs
        document.addEventListener('input', (e) => {
            if (e.target.matches('input[type="number"][data-currency]')) {
                this.formatCurrencyInput(e.target);
            }
        });
    }

    initializeComponents() {
        // Initialize tooltips
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });

        // Initialize popovers
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });

        // Setup chart themes
        if (typeof Chart !== 'undefined') {
            this.setupChartDefaults();
        }
    }

    setupDeleteModal(trigger) {
        const modal = document.getElementById('deleteModal');
        const confirmBtn = document.getElementById('confirmDelete');
        const deleteUrl = trigger.getAttribute('data-delete-url');
        const itemName = trigger.getAttribute('data-item-name') || 'este item';

        // Update modal content
        const modalBody = modal.querySelector('.modal-body p');
        modalBody.textContent = `Tem a certeza que pretende eliminar ${itemName}?`;

        // Setup confirm button
        confirmBtn.onclick = () => {
            if (deleteUrl) {
                // Create and submit form
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = deleteUrl;

                // Add CSRF token if available
                const csrfToken = document.querySelector('meta[name="_csrf"]');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
                
                if (csrfToken && csrfHeader) {
                    const csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = csrfHeader.getAttribute('content');
                    csrfInput.value = csrfToken.getAttribute('content');
                    form.appendChild(csrfInput);
                }

                // Add method override for DELETE
                const methodInput = document.createElement('input');
                methodInput.type = 'hidden';
                methodInput.name = '_method';
                methodInput.value = 'DELETE';
                form.appendChild(methodInput);

                document.body.appendChild(form);
                form.submit();
            }
        };
    }

    showLoading(form) {
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>A processar...';
            submitBtn.disabled = true;

            // Re-enable after 10 seconds as fallback
            setTimeout(() => {
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            }, 10000);
        }

        // Show loading modal if exists
        const loadingModal = document.getElementById('loadingModal');
        if (loadingModal) {
            const modal = new bootstrap.Modal(loadingModal);
            modal.show();
        }
    }

    setupFormValidation() {
        // Custom validation for forms
        document.addEventListener('submit', (e) => {
            if (e.target.matches('.needs-validation')) {
                if (!e.target.checkValidity()) {
                    e.preventDefault();
                    e.stopPropagation();
                }
                e.target.classList.add('was-validated');
            }
        });

        // Real-time validation feedback
        document.addEventListener('input', (e) => {
            if (e.target.form && e.target.form.classList.contains('was-validated')) {
                if (e.target.checkValidity()) {
                    e.target.classList.remove('is-invalid');
                    e.target.classList.add('is-valid');
                } else {
                    e.target.classList.remove('is-valid');
                    e.target.classList.add('is-invalid');
                }
            }
        });
    }

    setupToasts() {
        // Auto-show toasts
        document.addEventListener('DOMContentLoaded', () => {
            const toasts = document.querySelectorAll('.toast');
            toasts.forEach(toast => {
                const bsToast = new bootstrap.Toast(toast);
                bsToast.show();
            });
        });
    }

    autoHideAlerts() {
        const alerts = document.querySelectorAll('.alert[data-auto-hide]');
        alerts.forEach(alert => {
            const delay = parseInt(alert.getAttribute('data-auto-hide')) || 5000;
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, delay);
        });
    }

    formatCurrencyInput(input) {
        let value = input.value.replace(/[^\d.,]/g, '');
        value = value.replace(',', '.');
        
        // Ensure only one decimal point
        const parts = value.split('.');
        if (parts.length > 2) {
            value = parts[0] + '.' + parts.slice(1).join('');
        }
        
        // Limit to 2 decimal places
        if (parts.length === 2 && parts[1].length > 2) {
            value = parts[0] + '.' + parts[1].substring(0, 2);
        }
        
        input.value = value;
    }

    setupChartDefaults() {
        Chart.defaults.font.family = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
        Chart.defaults.plugins.legend.labels.usePointStyle = true;
        Chart.defaults.plugins.legend.labels.padding = 15;
        
        // Default colors
        Chart.defaults.backgroundColor = [
            '#2563eb', '#10b981', '#f59e0b', '#ef4444', 
            '#8b5cf6', '#06b6d4', '#84cc16', '#f97316'
        ];
    }

    // Utility functions
    showSuccessToast(message) {
        const toast = document.getElementById('successToast');
        const messageElement = document.getElementById('successMessage');
        
        if (toast && messageElement) {
            messageElement.textContent = message;
            const bsToast = new bootstrap.Toast(toast);
            bsToast.show();
        }
    }

    showErrorToast(message) {
        const toast = document.getElementById('errorToast');
        const messageElement = document.getElementById('errorMessage');
        
        if (toast && messageElement) {
            messageElement.textContent = message;
            const bsToast = new bootstrap.Toast(toast);
            bsToast.show();
        }
    }

    formatCurrency(amount, currency = 'EUR') {
        return new Intl.NumberFormat('pt-PT', {
            style: 'currency',
            currency: currency
        }).format(amount);
    }

    formatDate(date) {
        return new Intl.DateTimeFormat('pt-PT').format(new Date(date));
    }

    formatPercentage(value, decimals = 1) {
        return new Intl.NumberFormat('pt-PT', {
            style: 'percent',
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        }).format(value / 100);
    }

    // HTMX integration
    setupHTMXHandlers() {
        if (typeof htmx !== 'undefined') {
            // Show loading on HTMX requests
            document.addEventListener('htmx:beforeRequest', (e) => {
                const target = e.target;
                if (target.hasAttribute('data-loading')) {
                    target.classList.add('loading');
                }
            });

            // Hide loading after HTMX requests
            document.addEventListener('htmx:afterRequest', (e) => {
                const target = e.target;
                target.classList.remove('loading');
            });

            // Handle HTMX errors
            document.addEventListener('htmx:responseError', (e) => {
                this.showErrorToast('Ocorreu um erro. Tente novamente.');
            });
        }
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.MyFinances = new MyFinances();
});

// Export for module use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MyFinances;
}