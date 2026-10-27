import { Page, expect } from '@playwright/test';

export class InventoryPage {
  constructor(private readonly page: Page) {}

  async expectLoaded() {
    await expect(this.page).toHaveURL(/inventory\.html/);
    await expect(this.page.getByTestId('title')).toHaveText('Products');
  }

  items() {
    return this.page.getByTestId('inventory-item');
  }

  async sortBy(value: 'az' | 'za' | 'lohi' | 'hilo') {
    await this.page.getByTestId('product-sort-container').selectOption(value);
  }

  async prices(): Promise<number[]> {
    const texts = await this.page.getByTestId('inventory-item-price').allTextContents();
    return texts.map((t) => parseFloat(t.replace('$', '')));
  }

  async names(): Promise<string[]> {
    return this.page.getByTestId('inventory-item-name').allTextContents();
  }

  async addToCart(slug: string) {
    await this.page.getByTestId(`add-to-cart-${slug}`).click();
  }

  async removeFromCart(slug: string) {
    await this.page.getByTestId(`remove-${slug}`).click();
  }

  async openProduct(name: string) {
    await this.page.getByTestId('inventory-item-name').filter({ hasText: name }).click();
  }

  async openCart() {
    await this.page.getByTestId('shopping-cart-link').click();
  }

  cartBadge() {
    return this.page.getByTestId('shopping-cart-badge');
  }

  async logout() {
    await this.page.getByRole('button', { name: 'Open Menu' }).click();
    await this.page.getByTestId('logout-sidebar-link').click();
  }
}
