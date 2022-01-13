import {Overlay, OverlayConfig, OverlayRef} from '@angular/cdk/overlay';
import {ComponentPortal, ComponentType} from '@angular/cdk/portal';
import {ComponentRef, Injectable, Injector, SkipSelf, StaticProvider} from '@angular/core';
import {DialogContainer, DialogContainerComponent} from 'web/src/components/dialog/dialog.container.component';
import {DialogModule} from 'web/src/components/dialog/dialog.module';
import {LoginDialogComponent} from 'web/src/components/dialog/login/login.dialog.component';

import {DialogComponent} from '../components/dialog/dialog.component';
import {EditLinkComponent} from '../components/dialog/editlink/editlink.dialog.component';
import {Link} from '../models/link';
import {DIALOG_CONTAINER, LINK} from '../util/injectiontokens';


@Injectable({providedIn: DialogModule})
/** Dialog service that displays dialogs to the user. */
export class DialogService {
  constructor(
      private overlay: Overlay,
      @SkipSelf() private readonly injector: Injector,
  ) {}

  /**
   * Opens the login dialog component.
   * @return a reference to the open dialog.
   */
  showLoginDialog(): LoginDialogComponent {
    return this.attach(LoginDialogComponent);
  }

  showEditLinkDialog(link: Link): EditLinkComponent {
    return this.attach(EditLinkComponent, [{provide: LINK, useValue: link}]);
  }

  /**
   * Attaches the dialog component the the dom
   * @param dialog The component to open as a dialog.
   * @return a dialog instance
   */
  private attach<T extends DialogComponent<unknown>>(
      dialog: ComponentType<T>, providers?: StaticProvider[]): T {
    const overlayRef = this.createOverlay();
    const container = this.attachContainer(overlayRef);
    const injector = this.createInjector(container, providers);
    const dialogPortal = new ComponentPortal(dialog, null, injector);

    const ref = container.attachComponentPortal(dialogPortal);
    container.enter();
    container.onExit.subscribe(() => {
      overlayRef.detach();
      overlayRef.dispose();
    });
    return ref.instance;
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
    positionStrategy.centerVertically();

    overlayConfig.positionStrategy = positionStrategy;
    return this.overlay.create(overlayConfig);
  }

  /**
   * Attaches the dialog's container to the overlay.
   * @param overlayRef
   * @return The dialog container
   */
  private attachContainer(overlayRef: OverlayRef): DialogContainer {
    const containerPortal =
        new ComponentPortal(DialogContainerComponent, null, null);
    const containerRef: ComponentRef<DialogContainer> =
        overlayRef.attach(containerPortal);
    return containerRef.instance;
  }

  /**
   * Creates an injector with the given dialog container ref.
   * @param containerRef - The dialog's container.
   * @return An injector that can instantiate the dialog.
   */
  private createInjector(
      containerRef: DialogContainer,
      providers: StaticProvider[] = []): Injector {
    return Injector.create({
      parent: this.injector,
      providers:
          [{provide: DIALOG_CONTAINER, useValue: containerRef}, ...providers],
    });
  }
}
