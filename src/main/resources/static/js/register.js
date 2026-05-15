(function () {
  'use strict';

  const form = document.getElementById('register-form');
  if (!form) {
    return;
  }

  const PASSWORD_RE = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=*~_]).{8,30}$/;
  const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  function setFieldState(input, valid, message) {
    const err = form.querySelector('[data-error-for="' + input.id + '"]');
    input.classList.toggle('input-invalid', !valid);
    input.classList.toggle('input-valid', valid);
    if (err) {
      err.textContent = valid ? '' : message;
    }
  }

  function validateEmail() {
    const input = form.querySelector('#email');
    const value = input.value.trim();
    if (!value) {
      setFieldState(input, false, form.dataset.emailRequired || 'Email is required');
      return false;
    }
    if (!EMAIL_RE.test(value)) {
      setFieldState(input, false, form.dataset.emailInvalid || 'Invalid email');
      return false;
    }
    setFieldState(input, true, '');
    return true;
  }

  function validatePassword() {
    const input = form.querySelector('#password');
    const value = input.value;
    if (!value) {
      setFieldState(input, false, form.dataset.passwordRequired || 'Password is required');
      return false;
    }
    if (!PASSWORD_RE.test(value)) {
      setFieldState(input, false, form.dataset.passwordInvalid || 'Password too weak');
      return false;
    }
    setFieldState(input, true, '');
    return true;
  }

  function validateName() {
    const input = form.querySelector('#name');
    const value = input.value.trim();
    if (value.length < 2) {
      setFieldState(input, false, form.dataset.nameInvalid || 'Name is too short');
      return false;
    }
    setFieldState(input, true, '');
    return true;
  }

  form.querySelector('#email')?.addEventListener('input', validateEmail);
  form.querySelector('#email')?.addEventListener('blur', validateEmail);
  form.querySelector('#password')?.addEventListener('input', validatePassword);
  form.querySelector('#password')?.addEventListener('blur', validatePassword);
  form.querySelector('#name')?.addEventListener('input', validateName);
  form.querySelector('#name')?.addEventListener('blur', validateName);

  form.addEventListener('submit', (e) => {
    const ok = validateEmail() & validatePassword() & validateName();
    if (!ok) {
      e.preventDefault();
      window.BookStore?.showToast(form.dataset.formInvalid || 'Fix validation errors', 'error');
    }
  });
})();
