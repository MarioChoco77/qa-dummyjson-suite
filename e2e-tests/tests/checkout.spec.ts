import { test, expect } from '../pages/fixtures';

test.describe('Checkout', () => {
  test('flujo completo: agregar productos, pagar y confirmar orden @smoke', async ({
    loggedInInventory,
    checkoutPage,
    page,
  }) => {
    await loggedInInventory.addToCart('sauce-labs-backpack');
    await loggedInInventory.addToCart('sauce-labs-bolt-t-shirt');
    await expect(loggedInInventory.cartBadge()).toHaveText('2');

    await loggedInInventory.openCart();
    await expect(page.getByTestId('inventory-item')).toHaveCount(2);

    await checkoutPage.startFromCart();
    await checkoutPage.fillInformation('Ana', 'Pérez', '86000');

    // Resumen: subtotal + impuesto = total
    const { subtotal, tax, total } = await checkoutPage.readTotals();
    expect(subtotal).toBeCloseTo(29.99 + 15.99, 2);
    expect(subtotal + tax).toBeCloseTo(total, 2);

    await checkoutPage.finish();
    await checkoutPage.expectOrderComplete();
  });

  test('no permite continuar sin nombre', async ({ loggedInInventory, checkoutPage }) => {
    await loggedInInventory.addToCart('sauce-labs-onesie');
    await loggedInInventory.openCart();
    await checkoutPage.startFromCart();
    await checkoutPage.fillInformation('', 'Pérez', '86000');
    await checkoutPage.expectError('First Name is required');
  });

  test('no permite continuar sin código postal', async ({ loggedInInventory, checkoutPage }) => {
    await loggedInInventory.addToCart('sauce-labs-onesie');
    await loggedInInventory.openCart();
    await checkoutPage.startFromCart();
    await checkoutPage.fillInformation('Ana', 'Pérez', '');
    await checkoutPage.expectError('Postal Code is required');
  });

  test('cancelar el checkout regresa al carrito con los productos', async ({
    loggedInInventory,
    page,
    checkoutPage,
  }) => {
    await loggedInInventory.addToCart('sauce-labs-backpack');
    await loggedInInventory.openCart();
    await checkoutPage.startFromCart();
    await page.getByTestId('cancel').click();
    await expect(page).toHaveURL(/cart\.html/);
    await expect(page.getByTestId('inventory-item')).toHaveCount(1);
  });
});
