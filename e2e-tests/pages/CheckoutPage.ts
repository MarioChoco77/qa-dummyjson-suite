import { Page, expect } from '@playwright/test';

export class CheckoutPage {
  constructor(private readonly page: Page) {}

  async startFromCart() {
    await expect(this.page).toHaveURL(/cart\.html/);
    await this.page.getByTestId('checkout').click();
  }

  async fillInformation(firstName: string, lastName: string, postalCode: string) {
    await this.page.getByTestId('firstName').fill(firstName);
    await this.page.getByTestId('lastName').fill(lastName);
    await this.page.getByTestId('postalCode').fill(postalCode);
    await this.page.getByTestId('continue').click();
  }

  async finish() {
    await expect(this.page).toHaveURL(/checkout-step-two\.html/);
    await this.page.getByTestId('finish').click();
  }

  async expectOrderComplete() {
    await expect(this.page).toHaveURL(/checkout-complete\.html/);
    await expect(this.page.getByTestId('complete-header')).toHaveText('Thank you for your order!');
  }

  async expectError(message: string) {
    await expect(this.page.getByTestId('error')).toContainText(message);
  }

  /** Lee los importes del resumen para validar que subtotal + impuesto = total. */
  async readTotals() {
    const parse = async (testId: string) =>
      parseFloat(((await this.page.getByTestId(testId).textContent()) ?? '').replace(/[^0-9.]/g, ''));
    return {
      subtotal: await parse('subtotal-label'),
      tax: await parse('tax-label'),
      total: await parse('total-label'),
    };
  }
}
