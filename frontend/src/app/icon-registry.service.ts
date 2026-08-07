import { Injectable } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { MatIconRegistry } from '@angular/material/icon';

/**
 * Registers the Santander brand SVGs (pix, pay, transfer, account, cards,
 * credit, insurance) so templates can render them via <mat-icon svgIcon="pix">.
 */
@Injectable({ providedIn: 'root' })
export class BrandIconService {
  private readonly icons = [
    'pix',
    'pay',
    'transfer',
    'account',
    'cards',
    'credit',
    'insurance',
  ];

  constructor(registry: MatIconRegistry, sanitizer: DomSanitizer) {
    for (const name of this.icons) {
      registry.addSvgIcon(
        name,
        sanitizer.bypassSecurityTrustResourceUrl(`assets/icons/${name}.svg`)
      );
    }
  }
}
