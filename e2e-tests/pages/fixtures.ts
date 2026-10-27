import { test as base, expect } from '@playwright/test';
import { LoginPage } from './LoginPage';
import { InventoryPage } from './InventoryPage';
import { CheckoutPage } from './CheckoutPage';

export const USERS = {
  standard: { username: 'standard_user', password: 'secret_sauce' },
  locked: { username: 'locked_out_user', password: 'secret_sauce' },
};

type Fixtures = {
  loginPage: LoginPage;
  inventoryPage: InventoryPage;
  checkoutPage: CheckoutPage;
  /** Sesión iniciada con el usuario estándar, lista en /inventory.html */
  loggedInInventory: InventoryPage;
};

export const test = base.extend<Fixtures>({
  loginPage: async ({ page }, use) => use(new LoginPage(page)),
  inventoryPage: async ({ page }, use) => use(new InventoryPage(page)),
  checkoutPage: async ({ page }, use) => use(new CheckoutPage(page)),
  loggedInInventory: async ({ page }, use) => {
    const login = new LoginPage(page);
    const inventory = new InventoryPage(page);
    await login.goto();
    await login.login(USERS.standard.username, USERS.standard.password);
    await inventory.expectLoaded();
    await use(inventory);
  },
});

export { expect };
