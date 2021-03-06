import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';

interface Theme {
  '--background': string;
  '--foreground': string;
  '--anchor': string;
  '--accent': string;
  '--dim': string;
  '--bright': string;
  '--dark': string;
  '--error': string;
  '--success': string;
}

const light: Theme = {
  '--background': '#eeeef1',
  '--foreground': '#6b717f',
  '--anchor': '#0091ff',
  '--accent': '#f77d00',
  '--dim': '#959595',
  '--bright': '#d1d1d1',
  '--dark': '#4a4f5a',
  '--error': '#e93f3f',
  '--success': '#3fe99b',
};

const nord: Theme = {
  '--background': '#2e3440',
  '--foreground': '#d8dee9',
  '--anchor': '#bf616a',
  '--accent': '#ebcb8b',
  '--dim': '#aeb3bb',
  '--bright': '#eceff4',
  '--dark': '#3b4252',
  '--error': '#94545d',
  '--success': '#aebe8c',
};

const gruvbox: Theme = {
  '--background': '#282828',
  '--foreground': '#ebdbb2',
  '--anchor': '#ff524a',
  '--accent': '#d79921',
  '--dim': '#9d8d7d',
  '--bright': '#ebdbb2',
  '--dark': '#282828',
  '--error': '#ff524a',
  '--success': '#689d6a',
};

const everforest: Theme = {
  '--background': '#2f383e',
  '--foreground': '#d3c6aa',
  '--anchor': '#dbbc7f',
  '--accent': '#e69875',
  '--dim': '#7a8478',
  '--bright': '#d3c6aa',
  '--dark': '#2f383e',
  '--error': '#e67e80',
  '--success': '#a7c080',
}

@Injectable({providedIn: 'root'}) export class UiService {
  private readonly filterPanel$: ReplaySubject<boolean> = new ReplaySubject(1);

  private readonly themes: Map<string, Theme> = new Map([
    ['light', light],
    ['nord', nord],
    ['gruvbox', gruvbox],
    ['everforest', everforest],
  ]);

  private getTheme(theme?: string): Theme {
    if (theme && this.themes.has(theme)) {
      return this.themes.get(theme);
    }
    return this.themes.get('light');
  }

  getAllThemes(): Map<string, Theme> {
    return new Map(this.themes);
  }

  setPageTheme(theme: string): void {
    const loadedTheme = this.getTheme(theme);
    const root: HTMLElement = document.querySelector(':root');

    for (const [key, value] of Object.entries(loadedTheme)) {
      root.style.setProperty(key, value);
    }
  }

  getFilterPanelAsObservable(): Observable<boolean> {
    return this.filterPanel$.asObservable();
  }

  setFilterPanelVisible(visible: boolean): void {
    this.filterPanel$.next(visible);
  }
}
