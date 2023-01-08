import { Injectable } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';

interface Theme {
  '--background': string;
  '--background-alt': string;
  '--text': string;
  '--text-alt': string;
  '--anchor': string;
  '--icons': string;
  '--accent': string;
  '--tags': string;
  '--borders': string;
  '--shadows': string;
  '--error': string;
  '--warn': string;
  '--success': string;
}

const light: Theme = {
  '--background': '#eff8ff',
  '--background-alt': '#edf2f7',
  '--text': '#3a3f58',
  '--text-alt': '#5a5d6a',
  '--anchor': '#1067b1',
  '--icons': '#4ea1e9',
  '--accent': '#6b879b',
  '--tags': '#0c4c82',
  '--borders': '#93938e',
  '--shadows': '#a8b1cf',
  '--error': '#ee6a59',
  '--warn': '#FCCC5B',
  '--success': '#acd5bd',
};

const nightfox: Theme = {
  '--background': '#192330',
  '--background-alt': '#212e3f',
  '--text': '#cdcecf',
  '--text-alt': '#aeafb0',
  '--anchor': '#8dbdff',
  '--icons': '#8dbdff',
  '--accent': '#d671b2',
  '--tags': '#d671b2',
  '--borders': '#39506d',
  '--shadows': '#131a24',
  '--error': '#a52d4b',
  '--warn': '#f4a261',
  '--success': '#176e3b',
};

@Injectable({ providedIn: 'root' })
export class UiService {
  private readonly filterPanelVisibility$: ReplaySubject<boolean> =
    new ReplaySubject(1);
  private readonly pageSize$: ReplaySubject<number> = new ReplaySubject(1);

  private readonly themes: Map<string, Theme> = new Map([
    ['light', light],
    ['nightfox', nightfox],
  ]);

  /**
   * Returns the requested theme if it exists, otherwise the default theme.
   *
   * @param   {string} theme the name of the requested theme
   * @returns {Theme}        the requested theme or the default theme if the
   *                         requested one does not exist.
   */
  private getTheme(theme?: string): Theme {
    if (theme && this.themes.has(theme)) {
      return this.themes.get(theme);
    }
    return this.themes.get('light');
  }

  /**
   * Assembles a Map of the available themes and their names.
   *
   * @returns {Map<string,Theme>} a map of the theme names / themes.
   */
  getAllThemes(): Map<string, Theme> {
    return new Map(this.themes);
  }

  /**
   * Changes the current application color theme.
   * Note: if the theme does not exist the default theme will be used.
   *
   * @param {string} theme the name of the color theme to change to.
   */
  setPageTheme(theme: string): void {
    const loadedTheme = this.getTheme(theme);
    const root: HTMLElement = document.querySelector(':root');

    for (const [key, value] of Object.entries(loadedTheme)) {
      root.style.setProperty(key, value);
    }
  }

  /**
   * Gets the current link list page size as an Observable stream.
   *
   * @returns {Observable<number>} stream of the current page size.
   */
  getPageSizeAsObservable(): Observable<number> {
    return this.pageSize$.asObservable();
  }

  /**
   * Sets the current link list page size.
   * Note: this might trigger a new data fetch.
   *
   * @param {number} size the next page size to use.
   */
  setPageSize(size: number): void {
    this.pageSize$.next(size);
  }

  /**
   * Gets an Observable stream of the filter panel's current visibility.
   *
   * @returns {Observable<boolean>} stream of the filter panel's current
   *                                visibility.
   */
  getFilterPanelVisibilityAsObservable(): Observable<boolean> {
    return this.filterPanelVisibility$.asObservable();
  }

  /**
   * Sets the current visibility state of the filter panel.
   *
   * @param {boolean} visible the visibility state to set.
   */
  setFilterPanelVisible(visible: boolean): void {
    this.filterPanelVisibility$.next(visible);
  }
}
