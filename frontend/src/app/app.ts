import { Component } from '@angular/core';
import { routeAnimations } from './route-animation';
import { BrandIconService } from './icon-registry.service';
import { AuthService } from './auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css',
  animations: [routeAnimations],
})
export class App {
  protected readonly title = 'Santander Dev Week';

  // Exposed for the toolbar's logout button.
  constructor(
    public authService: AuthService,
    private router: Router,
    icons: BrandIconService
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
