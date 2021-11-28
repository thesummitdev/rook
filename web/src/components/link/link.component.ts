import {AfterViewInit, Component, Input} from '@angular/core';
import {take} from 'rxjs';
import {Link} from 'web/src/models/link';
import {DataService} from 'web/src/services/data.service';
import {ToastService} from 'web/src/services/toast.service';


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
  ) {}

  ngAfterViewInit() {
    this.tags = this.link.tags.split(' ');
  }


  onDelete(event: MouseEvent): void {
    event.stopPropagation();
    this.data.deleteLink(this.link).pipe(take(1)).subscribe(() => {
      this.toast.showWarning('Successfully removed!');
    });
  }
}
