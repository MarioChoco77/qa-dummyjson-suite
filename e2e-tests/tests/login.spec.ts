import { test, expect, USERS } from '../pages/fixtures';

test.describe('Login', () => {
  test('usuario válido accede al catálogo @smoke', async ({ loginPage, inventoryPage }) => {
    await loginPage.goto();
    await loginPage.login(USERS.standard.username, USERS.standard.password);
    await inventoryPage.expectLoaded();
    await expect(inventoryPage.items()).toHaveCount(6);
  });

  test('contraseña incorrecta muestra mensaje de error', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.login(USERS.standard.username, 'clave_incorrecta');
    await loginPage.expectError('Username and password do not match');
    await expect(page).not.toHaveURL(/inventory\.html/);
  });

  test('usuario bloqueado no puede iniciar sesión', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login(USERS.locked.username, USERS.locked.password);
    await loginPage.expectError('this user has been locked out');
  });

  test('campos vacíos exigen usuario', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login('', '');
    await loginPage.expectError('Username is required');
  });

  test('cerrar sesión devuelve a la pantalla de login', async ({ loggedInInventory, page }) => {
    await loggedInInventory.logout();
    await expect(page).toHaveURL('/');
    await expect(page.getByTestId('login-button')).toBeVisible();
  });

  test('acceso directo al catálogo sin sesión es rechazado', async ({ page, loginPage }) => {
    await page.goto('/inventory.html');
    await expect(page).toHaveURL('/');
    await loginPage.expectError("You can only access '/inventory.html' when you are logged in");
  });
});
