(function () {
  'use strict';

  const CSRF_META = () => ({
    token: document.querySelector('meta[name="_csrf"]')?.content,
    header: document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN',
  });

  function showToast(message, type) {
    const root = document.getElementById('toast-root');
    if (!root || !message) {
      return;
    }
    const el = document.createElement('div');
    el.className = 'toast toast--' + (type === 'error' ? 'error' : 'success');
    el.setAttribute('role', 'status');
    el.textContent = message;
    root.appendChild(el);
    requestAnimationFrame(() => el.classList.add('toast--visible'));
    setTimeout(() => {
      el.classList.remove('toast--visible');
      setTimeout(() => el.remove(), 300);
    }, 4200);
  }

  function initFlashToasts() {
    const success = document.getElementById('flash-success');
    const error = document.getElementById('flash-error');
    if (success?.textContent?.trim()) {
      showToast(success.textContent.trim(), 'success');
    }
    if (error?.textContent?.trim()) {
      showToast(error.textContent.trim(), 'error');
    }
  }

  async function fetchJson(url, options) {
    const csrf = CSRF_META();
    const headers = Object.assign({ Accept: 'application/json' }, options?.headers || {});
    if (csrf.token && options?.method && options.method !== 'GET') {
      headers[csrf.header] = csrf.token;
    }
    const response = await fetch(url, Object.assign({}, options, { headers }));
    if (!response.ok) {
      throw new Error('HTTP ' + response.status);
    }
    return response.json();
  }

  async function postForm(url, formData) {
    const csrf = CSRF_META();
    const headers = { Accept: 'application/json' };
    if (csrf.token) {
      headers[csrf.header] = csrf.token;
    }
    const response = await fetch(url, { method: 'POST', headers, body: formData });
    if (!response.ok) {
      throw new Error('HTTP ' + response.status);
    }
    return response.json();
  }

  function formatBasketLabel(template, count) {
    if (!template) {
      return String(count);
    }
    return template.replace('{0}', String(count));
  }

  async function refreshBasketBadge() {
    const badge = document.getElementById('basket-count');
    if (!badge) {
      return;
    }
    try {
      const data = await fetchJson('/app/basket/summary');
      const qty = data.totalQuantity || 0;
      const template = badge.dataset.labelTemplate || '({0})';
      if (qty > 0) {
        badge.textContent = formatBasketLabel(template, qty);
        badge.hidden = false;
      } else {
        badge.textContent = '';
        badge.hidden = true;
      }
    } catch (e) {
      console.warn('Basket summary failed', e);
    }
  }

  function initAjaxBasketForms() {
    document.querySelectorAll('form.js-basket-add').forEach((form) => {
      form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const submitBtn = form.querySelector('[type="submit"]');
        if (submitBtn) {
          submitBtn.disabled = true;
        }
        try {
          const body = new FormData(form);
          body.append('ajax', 'true');
          await postForm(form.action, body);
          await refreshBasketBadge();
          const msg = form.dataset.successMessage || 'Added to basket';
          showToast(msg, 'success');
        } catch (e) {
          console.error(e);
          const msg = form.dataset.errorMessage || 'Could not update basket';
          showToast(msg, 'error');
        } finally {
          if (submitBtn) {
            submitBtn.disabled = false;
          }
        }
      });
    });
  }

  window.BookStore = {
    showToast,
    fetchJson,
    postForm,
    refreshBasketBadge,
    CSRF_META,
  };

  function initNavActive() {
    const nav = document.getElementById('site-nav');
    if (!nav) {
      return;
    }
    const path = window.location.pathname.replace(/\/$/, '') || '/';
    nav.querySelectorAll('a[href]').forEach((link) => {
      const href = link.getAttribute('href');
      if (!href || href.startsWith('http')) {
        return;
      }
      const linkPath = href.replace(/\/$/, '') || '/';
      const matches =
        path === linkPath || (linkPath !== '/' && path.startsWith(linkPath + '/'));
      link.classList.toggle('is-active', matches);
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    initFlashToasts();
    refreshBasketBadge();
    initAjaxBasketForms();
    initNavActive();
  });
})();
