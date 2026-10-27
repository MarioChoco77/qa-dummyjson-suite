import { test, expect } from '../pages/fixtures';

test.describe('Navegación del catálogo', () => {
  test('ordenar por precio de menor a mayor @smoke', async ({ loggedInInventory }) => {
    await loggedInInventory.sortBy('lohi');
    const prices = await loggedInInventory.prices();
    expect(prices).toEqual([...prices].sort((a, b) => a - b));
  });

  test('ordenar por nombre Z-A', async ({ loggedInInventory }) => {
    await loggedInInventory.sortBy('za');
    const names = await loggedInInventory.names();
    expect(names).toEqual([...names].sort().reverse());
  });

  test('abrir el detalle de un producto y volver al catálogo', async ({ loggedInInventory, page }) => {
    await loggedInInventory.openProduct('Sauce Labs Backpack');
    await expect(page).toHaveURL(/inventory-item\.html\?id=4/);
    await expect(page.getByTestId('inventory-item-name')).toHaveText('Sauce Labs Backpack');
    await page.getByTestId('back-to-products').click();
    await loggedInInventory.expectLoaded();
  });

  test('el contador del carrito refleja altas y bajas', async ({ loggedInInventory }) => {
    await loggedInInventory.addToCart('sauce-labs-backpack');
    await loggedInInventory.addToCart('sauce-labs-bike-light');
    await expect(loggedInInventory.cartBadge()).toHaveText('2');

    await loggedInInventory.removeFromCart('sauce-labs-backpack');
    await expect(loggedInInventory.cartBadge()).toHaveText('1');

    await loggedInInventory.removeFromCart('sauce-labs-bike-light');
    await expect(loggedInInventory.cartBadge()).toHaveCount(0);
  });
});
