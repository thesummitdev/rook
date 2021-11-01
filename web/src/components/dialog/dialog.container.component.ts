import {AnimationEvent} from '@angular/animations';
import {BasePortalOutlet, CdkPortalOutlet, ComponentPortal, TemplatePortal} from '@angular/cdk/portal';
import {Component, ComponentRef, EmbeddedViewRef, HostBinding, HostListener, NgZone, OnDestroy, ViewChild} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {take} from 'rxjs/operators';
import {dialogAnimations} from './dialog.animations';

export interface DialogContainer {
  readonly onExit: Subject<any>;
  readonly onEnter: Subject<any>;
  enter: () => void;
  exit: () => void;
  attachTemplatePortal: <C>(portal: TemplatePortal<C>) => EmbeddedViewRef<C>;
  attachComponentPortal: <T>(portal: ComponentPortal<T>) => ComponentRef<T>;
}

@Component({
  selector: 'dialog-container',
  templateUrl: 'dialog.container.component.html',
  styleUrls: ['./dialog.container.component.scss'],
  animations: [dialogAnimations.dialogState],
})
export class DialogContainerComponent extends BasePortalOutlet implements
    OnDestroy, DialogContainer {
  /** Subject for notifying that the snack bar has exited from view. */
  readonly onExit: Subject<void> = new Subject();

  /**
   * Subject for notifying that the snack bar has finished entering the view.
   */
  readonly onEnter: Subject<void> = new Subject();
  private internalAnimationState = 'void';

  @HostBinding('class') readonly classes = 'dialog-container';

  @HostBinding('@state')
  get animationState(): string {
    return this.internalAnimationState;
  }

  /**
   * The portal outlet inside of this container into which the toast content
   * will be loaded.
   */
  @ViewChild(CdkPortalOutlet, {static: true}) portalOutlet: CdkPortalOutlet;

  constructor(
      private ngZone: NgZone,
  ) {
    super();
  }

  ngOnDestroy(): void {
    this.completeExit();
  }

  attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T> {
    return this.portalOutlet.attachComponentPortal(portal);
  }
  attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C> {
    return this.portalOutlet.attachTemplatePortal(portal);
  }

  @HostListener('@state.done', ['$event'])
  /** Listens for the end of state animations. */
  onAnimationEnd(event: AnimationEvent) {
    const {fromState, toState} = event;
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

  /** Called when the container is ready to animate in. */
  enter(): void {
    this.internalAnimationState = 'visible';
  }

  /** Called when the container is ready to animate out. */
  exit(): Observable<void> {
    this.internalAnimationState = 'hidden';
    return this.onExit;
  }

  private completeExit() {
    this.ngZone.onMicrotaskEmpty.pipe(take(1)).subscribe(() => {
      this.onExit.next();
      this.onExit.complete();
    });
  }
}
