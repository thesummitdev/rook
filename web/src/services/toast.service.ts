import {Overlay, OverlayConfig, OverlayRef} from '@angular/cdk/overlay';
import {ComponentPortal} from '@angular/cdk/portal';
import {ComponentRef, Injectable, Injector, SkipSelf} from '@angular/core';
import {ToastComponent} from 'web/src/components/toast/toast.component';
import {ToastConfig} from 'web/src/components/toast/toast.config';
import {ToastContainer, ToastContainerComponent} from 'web/src/components/toast/toast.container.component';
import {ToastModule} from 'web/src/components/toast/toast.module';
import {Semantic} from 'web/src/util/enums';
import {TOAST_CONFIG} from 'web/src/util/injectiontokens';


@Injectable({providedIn: ToastModule})
/** Toast service that displays messages to the user. */
export class ToastService {
  constructor(
      private overlay: Overlay,
      @SkipSelf() private readonly injector: Injector,
  ) {}

  /**
   * Shows a toast info message for the specified duration.
   * @param message
   * @param duration - Duration in milliseconds to show the toast.
   */
  showMessage(message: string, duration: number = 3000): void {
    this.attach({message, semantic: Semantic.INFO, duration});
  }

  /**
   * Shows a toast warning message for the specified duration.
   * @param message
   * @param duration - Duration in milliseconds to show the toast.
   */
  showWarning(message: string, duration: number = 3000): void {
    this.attach({message, semantic: Semantic.WARN, duration});
  }

  /**
   * Shows a toast error message for the specified duration.
   * @param message
   * @param duration - Duration in milliseconds to show the toast.
   */
  showError(message: string, duration: number = 3000): void {
    this.attach({message, semantic: Semantic.ERROR, duration});
  }

  /**
   * Attaches the toast the the dom, and dismisses the toast after the
   * configured duration.
   * @param config The config objectg for the toast to attach.
   */
  private attach(config: ToastConfig = {
    message: '',
    semantic: Semantic.UNDEFINED,
    duration: 3000,
  }): void {
    const overlayRef = this.createOverlay();
    const container = this.attachContainer(overlayRef);
    const injector = this.createInjector(config);
    const toastPortal = new ComponentPortal(ToastComponent, null, injector);

    container.attachComponentPortal(toastPortal);
    container.enter();
    const exited = container.dismissAfter(config.duration);
    exited.subscribe(() => {
      overlayRef.detach();
      overlayRef.dispose();
    });
  }

  /**
   * Creates a new overlay and places it in the correct location.
   * @return a reference to the overlay.
   */
  private createOverlay(): OverlayRef {
    const overlayConfig = new OverlayConfig();
    overlayConfig.direction = 'ltr';

    const positionStrategy = this.overlay.position().global();
    positionStrategy.centerHorizontally();
    positionStrategy.bottom('0');

    overlayConfig.positionStrategy = positionStrategy;
    return this.overlay.create(overlayConfig);
  }

  /**
   * Attaches the toast's container to the overlay.
   * @param overlayRef
   * @return The toast container
   */
  private attachContainer(overlayRef: OverlayRef): ToastContainer {
    const toastPortal =
        new ComponentPortal(ToastContainerComponent, null, null);
    const containerRef: ComponentRef<ToastContainer> =
        overlayRef.attach(toastPortal);
    return containerRef.instance;
  }

  /**
   * Creates an injector with the given Toast config.
   * @param config - The current Toast message's config.
   * @return An injector that can instantiate the toast.
   */
  private createInjector(config: ToastConfig): Injector {
    const injectionTokens = new WeakMap();

    injectionTokens.set(TOAST_CONFIG, config);

    return Injector.create({
      parent: this.injector,
      providers: [
        {provide: TOAST_CONFIG, useValue: config},
      ],
    });
  }
}
