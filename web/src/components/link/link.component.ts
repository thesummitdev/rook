import { AfterViewInit, Component, Input } from '@angular/core';
import { EMPTY, switchMap, take } from 'rxjs';
import { Link } from 'web/src/models/link';
import { DataService } from 'web/src/services/data.service';
import { DialogService } from 'web/src/services/dialog.service';
import { FilterService } from 'web/src/services/filters.service';
import { ToastService } from 'web/src/services/toast.service';

@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.scss'],
})
export class LinkComponent implements AfterViewInit {
  @Input() link: Link;
  tags: string[] = [];

  constructor(
    private readonly data: DataService,
    private readonly toast: ToastService,
    private readonly dialog: DialogService,
    private readonly filters: FilterService
  ) {}

  /**
   *
   */
  ngAfterViewInit() {
    this.tags = this.link.tags.split(' ');
  }

  /**
   * Deletes the current link from the database.
   *
   * @param event - the click event (event is swallowed)
   */
  onDelete(event: MouseEvent): void {
    event.stopPropagation();
    this.data
      .deleteLink(this.link)
      .pipe(take(1))
      .subscribe(() => {
        this.toast.showWarning('Successfully removed!');
      });
  }

  /**
   *
   * @param tag
   */
  addTag(tag: string) {
    this.filters
      .getTagsAsObservable()
      .pipe(take(1))
      .subscribe(tags => {
        this.filters.setTags(tags.add(tag));
      });
  }

  /**
   * Opens the edit dialog.
   *
   * @param event - the click event (event is swallowed)
   */
  onEdit(event: MouseEvent): void {
    event.stopPropagation();
    const updatedLink = this.dialog
      .showEditLinkDialog(this.link)
      .resultAsObservable()
      .pipe(
        switchMap(dialog => {
          if (dialog.result) {
            return this.data.updateLink(dialog.result);
          }
          return EMPTY;
        })
      );

    updatedLink.subscribe(updated => {
      if (updated) {
        this.link = updated;
        this.tags = updated.tags.split(' ');
      }
    });
  }

  /**
   * Copies the current link to the clipboard.
   * NOTE: the clipboard API is only available in Secure (https) or dev mode
   * (localhost)
   *
   * @param event - the click event (event is swallowed)
   */
  onCopy(event: MouseEvent): void {
    event.stopPropagation();
    navigator.clipboard.writeText(this.link.url).then(
      // Success
      () => {
        this.toast.showMessage('Copied!');
      },
      // Error
      () => {
        this.toast.showError('Something bad happened.');
      }
    );
  }
}
