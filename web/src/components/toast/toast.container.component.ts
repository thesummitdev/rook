import {
  animate,
  AnimationEvent,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';
import {
  BasePortalOutlet,
  CdkPortalOutlet,
  ComponentPortal,
  TemplatePortal,
} from '@angular/cdk/portal';
import {
  Component,
  ComponentRef,
  EmbeddedViewRef,
  HostBinding,
  HostListener,
  NgZone,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { take } from 'rxjs/operators';
import { toastAnimations } from './toast.animations';

export interface ToastContainer {
  readonly onExit: Subject<any>;
  readonly onEnter: Subject<any>;
  enter: () => void;
  exit: () => Observable<void>;
  dismissAfter: (duration: number) => Observable<void>;
  attachTemplatePortal: <C>(portal: TemplatePortal<C>) => EmbeddedViewRef<C>;
  attachComponentPortal: <T>(portal: ComponentPortal<T>) => ComponentRef<T>;
}

@Component({
  selector: 'toast-container',
  templateUrl: 'toast.container.component.html',
  animations: [toastAnimations.toastState],
})
export class ToastContainerComponent
  extends BasePortalOutlet
  implements OnDestroy, ToastContainer
{
  /** Subject for notifying that the snack bar has exited from view. */
  readonly onExit: Subject<void> = new Subject();

  /**
   * Subject for notifying that the snack bar has finished entering the view.
   */
  readonly onEnter: Subject<void> = new Subject();
  private internalAnimationState = 'void';

  @HostBinding('class') readonly classes = 'toast-container';

  /**
   *
   */
  @HostBinding('@state')
  get animationState(): string {
    return this.internalAnimationState;
  }

  /**
   * The portal outlet inside of this container into which the toast content
   * will be loaded.
   */
  @ViewChild(CdkPortalOutlet, { static: true }) portalOutlet: CdkPortalOutlet;

  constructor(private ngZone: NgZone) {
    super();
  }

  /**
   *
   */
  ngOnDestroy(): void {
    this.completeExit();
  }

  /**
   *
   * @param portal
   */
  attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T> {
    return this.portalOutlet.attachComponentPortal(portal);
  }
  /**
   *
   * @param portal
   */
  attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C> {
    return this.portalOutlet.attachTemplatePortal(portal);
  }

  /**
   *
   * @param event
   */
  @HostListener('@state.done', ['$event'])
  onAnimationEnd(event: AnimationEvent) {
    const { fromState, toState } = event;
    if ((toState === 'void' && fromState !== 'void') || toState === 'hidden') {
      this.completeExit();
    }

    if (toState === 'visible') {
      // Note: we shouldn't use `this` inside the zone callback,
      // because it can cause a memory leak.
      const onEnter = this.onEnter;

      this.ngZone.run(() => {
        onEnter.next();
        onEnter.complete();
      });
    }
  }

  /**
   *
   */
  enter(): void {
    this.internalAnimationState = 'visible';
  }

  /**
   *
   */
  exit(): Observable<void> {
    this.internalAnimationState = 'hidden';
    return this.onExit;
  }

  /**
   *
   * @param duration
   */
  dismissAfter(duration: number): Observable<void> {
    setTimeout(() => {
      this.internalAnimationState = 'hidden';
    }, duration);
    return this.onExit;
  }

  /**
   *
   */
  private completeExit() {
    this.ngZone.onMicrotaskEmpty.pipe(take(1)).subscribe(() => {
      this.onExit.next();
      this.onExit.complete();
    });
  }
}
