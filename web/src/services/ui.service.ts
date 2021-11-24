import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';

@Injectable({providedIn: 'root'})
export class UiService {
  private readonly createPanel$: ReplaySubject<boolean> = new ReplaySubject(1);

  getCreatePanelAsObservable(): Observable<boolean> {
    return this.createPanel$.asObservable();
  }

  setCreatePanelVisible(visible: boolean): void {
    this.createPanel$.next(visible);
  }
}
