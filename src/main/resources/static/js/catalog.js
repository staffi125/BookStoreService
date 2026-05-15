(function () {
  'use strict';

  const root = document.getElementById('catalog-root');
  if (!root) {
    return;
  }

  const form = document.getElementById('catalog-filter-form');
  const grid = document.getElementById('book-grid');
  const meta = document.getElementById('catalog-meta');
  const pager = document.getElementById('catalog-pager');
  let debounceTimer;

  if (!form) {
    return;
  }

  function escapeHtml(text) {
    const el = document.createElement('div');
    el.textContent = text == null ? '' : String(text);
    return el.innerHTML;
  }

  function buildCard(book) {
    const pages =
      book.pages != null ? root.dataset.pagesLabel.replace('{0}', book.pages) : '';
    const price = book.price + ' ' + (root.dataset.currency || 'USD');
    let html = '<article class="card">';
    html += '<div class="card-cover" aria-hidden="true"></div>';
    html += '<h3>' + escapeHtml(book.name) + '</h3>';
    html += '<div class="meta">' + escapeHtml(book.author) + ' · ' + escapeHtml(book.genre) + '</div>';
    if (pages) {
      html += '<div class="meta">' + escapeHtml(pages) + '</div>';
    }
    html += '<div class="price">' + escapeHtml(price) + '</div>';
    html += '<div class="btn-row" style="margin-top:auto">';
    html += '<a class="btn btn-secondary" href="' + escapeHtml(book.detailUrl) + '">';
    html += escapeHtml(root.dataset.viewLabel || 'View') + '</a>';
    if (root.dataset.clientMode === 'true') {
      html +=
        '<button type="button" class="btn btn-primary js-catalog-quick-add" data-book="' +
        escapeHtml(book.name) +
        '">' +
        escapeHtml(root.dataset.addLabel || 'Add') +
        '</button>';
    }
    html += '</div></article>';
    return html;
  }

  function renderPager(data, params) {
    if (!pager) {
      return;
    }
    if (data.totalPages <= 1) {
      pager.innerHTML = '';
      pager.hidden = true;
      return;
    }
    pager.hidden = false;
    const q = new URLSearchParams(params);
    let html = '';
    if (data.hasPrevious) {
      q.set('page', String(data.page - 1));
      html +=
        '<a class="btn btn-secondary" href="/app/books?' +
        q.toString() +
        '">' +
        escapeHtml(root.dataset.prevLabel || 'Prev') +
        '</a>';
    }
    html +=
      '<span class="pager-gap">' +
      escapeHtml(
        (root.dataset.pageIndicator || '{0} / {1}')
          .replace('{0}', String(data.page + 1))
          .replace('{1}', String(data.totalPages)),
      ) +
      '</span>';
    if (data.hasNext) {
      q.set('page', String(data.page + 1));
      html +=
        '<a class="btn btn-secondary" href="/app/books?' +
        q.toString() +
        '">' +
        escapeHtml(root.dataset.nextLabel || 'Next') +
        '</a>';
    }
    pager.innerHTML = html;
  }

  function formParams() {
    const data = new FormData(form);
    const params = new URLSearchParams();
    data.forEach((value, key) => {
      if (value !== '' && value != null) {
        params.set(key, value);
      }
    });
    params.set('page', '0');
    return params;
  }

  async function runSearch() {
    if (!grid) {
      return;
    }
    const params = formParams();
    grid.classList.add('catalog-loading');
    try {
      const data = await window.BookStore.fetchJson('/app/books/search?' + params.toString());
      if (data.totalElements === 0) {
        grid.innerHTML =
          '<p class="meta">' + escapeHtml(root.dataset.emptyLabel || 'No books') + '</p>';
        if (meta) {
          meta.textContent = '';
          meta.hidden = true;
        }
      } else {
        grid.innerHTML = data.content.map(buildCard).join('');
        if (meta) {
          meta.hidden = false;
          meta.textContent = (root.dataset.pageStatus || '')
            .replace('{0}', String(data.page + 1))
            .replace('{1}', String(data.totalPages))
            .replace('{2}', String(data.totalElements));
        }
      }
      renderPager(data, params);
      bindQuickAdd();
    } catch (e) {
      console.error(e);
      window.BookStore.showToast(root.dataset.errorLabel || 'Search failed', 'error');
    } finally {
      grid.classList.remove('catalog-loading');
    }
  }

  function scheduleSearch() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(runSearch, 350);
  }

  function bindQuickAdd() {
    if (!grid) {
      return;
    }
    grid.querySelectorAll('.js-catalog-quick-add').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const body = new FormData();
        body.append('bookName', btn.dataset.book);
        body.append('quantity', '1');
        body.append('ajax', 'true');
        try {
          btn.disabled = true;
          await window.BookStore.postForm('/app/basket/add', body);
          await window.BookStore.refreshBasketBadge();
          window.BookStore.showToast(root.dataset.addedLabel || 'Added', 'success');
        } catch (e) {
          window.BookStore.showToast(root.dataset.addErrorLabel || 'Failed', 'error');
        } finally {
          btn.disabled = false;
        }
      });
    });
  }

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    runSearch();
  });

  form.querySelectorAll('input').forEach((input) => {
    input.addEventListener('input', scheduleSearch);
    input.addEventListener('change', scheduleSearch);
  });

  bindQuickAdd();
})();
