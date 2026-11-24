// Rendert die SVG-Grafiken aus docs/src als PNG (Playwright Chromium).
// Usage: node scripts/render-assets.js
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const assets = path.join(root, 'docs/src');
const jobs = [
  { src: 'icon.svg', out: 'docs/icon.png', w: 512, h: 512 },
  { src: 'banner.svg', out: 'docs/banner.png', w: 1280, h: 640 },
];

(async () => {
  const browser = await chromium.launch();
  for (const job of jobs) {
    const svg = fs.readFileSync(path.join(assets, job.src), 'utf8');
    const page = await browser.newPage({ viewport: { width: job.w, height: job.h }, deviceScaleFactor: 1 });
    await page.setContent(
      `<html><body style="margin:0;background:transparent">${svg}</body></html>`,
      { waitUntil: 'load' },
    );
    await page.evaluate(() => document.fonts.ready);
    fs.mkdirSync(path.dirname(path.join(root, job.out)), { recursive: true });
    await page.locator('svg').screenshot({ path: path.join(root, job.out), omitBackground: true });
    await page.close();
    console.log('rendered', job.out);
  }
  await browser.close();
})();
