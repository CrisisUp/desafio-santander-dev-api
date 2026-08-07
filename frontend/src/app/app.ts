import { Component } from '@angular/core';
import { routeAnimations } from './route-animation';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css',
  animations: [routeAnimations],
})
export class App {
  protected readonly title = 'Santander Dev Week';
}
