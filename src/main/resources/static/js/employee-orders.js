(function () {
  'use strict';

  const table = document.getElementById('employee-orders-table');
  if (!table) {
    return;
  }

  const tbody = table.querySelector('tbody');
  const staffEmail = table.dataset.staffEmail || '';
  const intervalMs = parseInt(table.dataset.refreshMs || '30000', 10);
  let lastSignature = '';

  function escapeHtml(text) {
    const el = document.createElement('div');
    el.textContent = text == null ? '' : String(text);
    return el.innerHTML;
  }

  function statusBadge(status) {
    const labels = {
      NEW: table.dataset.statusNew || 'New',
      CONFIRMED: table.dataset.statusConfirmed || 'Confirmed',
      SHIPPED: table.dataset.statusShipped || 'Shipped',
      DELIVERED: table.dataset.statusDelivered || 'Delivered',
      CANCELLED: table.dataset.statusCancelled || 'Cancelled',
    };
    const cls = {
      NEW: 'badge-new',
      CONFIRMED: 'badge-confirmed',
      SHIPPED: 'badge-shipped',
      DELIVERED: 'badge-delivered',
      CANCELLED: 'badge-cancelled',
    };
    const label = labels[status] || status;
    const css = cls[status] || 'badge-new';
    return '<span class="badge ' + css + '">' + escapeHtml(label) + '</span>';
  }

  function itemsCell(items) {
    if (!items || !items.length) {
      return '';
    }
    return items
      .map((it) => escapeHtml(it.bookName) + ' ×' + it.quantity)
      .join(', ');
  }

  function actionForms(order) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfName = document.querySelector('meta[name="_csrf_parameter"]')?.content || '_csrf';
    let html = '<div class="btn-row" style="flex-wrap:wrap;gap:0.35rem">';
    const canConfirm =
      order.status === 'NEW' && (!order.employeeEmail || order.employeeEmail === staffEmail);
    const canShip = order.status === 'CONFIRMED' && order.employeeEmail === staffEmail;
    const canDeliver = order.status === 'SHIPPED' && order.employeeEmail === staffEmail;

    function form(action, label, btnClass) {
      return (
        '<form class="inline-form" action="' +
        action +
        '" method="post">' +
        '<input type="hidden" name="' +
        escapeHtml(csrfName) +
        '" value="' +
        escapeHtml(csrfToken) +
        '">' +
        '<input type="hidden" name="id" value="' +
        order.id +
        '">' +
        '<button type="submit" class="btn ' +
        btnClass +
        '" style="font-size:0.85rem;padding:0.35rem 0.75rem">' +
        escapeHtml(label) +
        '</button></form>'
      );
    }

    if (canConfirm) {
      html += form('/app/employee/orders/confirm', table.dataset.confirmLabel || 'Confirm', 'btn-primary');
    }
    if (canShip) {
      html += form('/app/employee/orders/ship', table.dataset.shipLabel || 'Ship', 'btn-secondary');
    }
    if (canDeliver) {
      html += form(
        '/app/employee/orders/deliver',
        table.dataset.deliverLabel || 'Delivered',
        'btn-secondary',
      );
    }
    html += '</div>';
    return html;
  }

  function renderRow(order) {
    return (
      '<tr data-order-id="' +
      order.id +
      '">' +
      '<td class="mono">' +
      order.id +
      '</td>' +
      '<td>' +
      escapeHtml(order.clientEmail) +
      '</td>' +
      '<td>' +
      escapeHtml(order.employeeEmail || '-') +
      '</td>' +
      '<td>' +
      escapeHtml(order.orderDate) +
      '</td>' +
      '<td>' +
      escapeHtml(order.price) +
      '</td>' +
      '<td>' +
      statusBadge(order.status) +
      '</td>' +
      '<td>' +
      itemsCell(order.bookItems) +
      '</td>' +
      '<td>' +
      actionForms(order) +
      '</td>' +
      '</tr>'
    );
  }

  function signature(orders) {
    return orders.map((o) => o.id + ':' + o.status + ':' + (o.employeeEmail || '')).join('|');
  }

  async function refresh() {
    try {
      const orders = await window.BookStore.fetchJson('/app/employee/orders/data');
      const sig = signature(orders);
      if (sig !== lastSignature) {
        const hadRows = lastSignature !== '';
        tbody.innerHTML = orders.map(renderRow).join('');
        lastSignature = sig;
        if (hadRows) {
          window.BookStore.showToast(table.dataset.refreshedLabel || 'Orders updated', 'success');
        }
        table.classList.add('orders-refreshed');
        setTimeout(() => table.classList.remove('orders-refreshed'), 800);
      }
    } catch (e) {
      console.warn('Orders refresh failed', e);
    }
  }

  refresh();
  setInterval(refresh, intervalMs);
})();
