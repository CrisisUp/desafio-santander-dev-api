import { Component } from '@angular/core';
import { routeAnimations } from './route-animation';
import { BrandIconService } from './icon-registry.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css',
  animations: [routeAnimations],
})
export class App {
  protected readonly title = 'Santander Dev Week';

  // Registering the brand icons happens once, at app bootstrap.
  constructor(icons: BrandIconService) {}
}
