import {AfterViewInit, Component, Input} from '@angular/core';
import {Link} from 'web/src/models/link';


@Component({
  selector: 'app-link',
  templateUrl: './link.component.html',
  styleUrls: ['./link.component.scss'],
})
export class LinkComponent implements AfterViewInit {
  @Input() link: Link;
  tags: string[] = [];

  constructor() {}

  ngAfterViewInit() {
    this.tags = this.link.tags.split(' ');
  }
}
