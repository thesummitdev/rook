import { Pipe, PipeTransform } from '@angular/core';
import { DateTime } from 'luxon';

@Pipe({ name: 'timeSince' })
export class TimeSince implements PipeTransform {
  /**
   *
   * @param value
   */
  transform(value: number): string {
    let result = '';
    if (!value || value === 0) {
      return result;
    }

    const date = DateTime.fromMillis(value);
    // The difference will be a negative number, so invert it to positive.
    const difference = date.diffNow().as('minutes') * -1;

    if (difference <= 1) {
      result = 'Just now';
    } else if (difference < 3) {
      result = 'A moment ago';
    } else if (difference < 60) {
      result = `${Math.floor(difference)} minutes ago`;
    } else if (difference < 120) {
      result = 'A hour ago';
    } else if (difference < 1440) {
      const hours = Math.floor(difference / 60);
      result = `${hours} hours ago`;
    } else if (difference < 2880) {
      result = 'A day ago';
    } else if (difference < 10080) {
      const days = Math.floor(difference / 1440);
      result = `${days} days ago`;
    } else if (difference < 11520) {
      result = 'A week ago';
    } else if (difference < 40320) {
      const weeks = Math.floor(difference / 10080);
      result = `${weeks} weeks ago`;
    } else if (difference < 80640) {
      result = 'A month ago';
    } else if (difference < 525600) {
      const months = Math.floor(difference / 40320);
      result = `${months} months ago`;
    } else {
      result = `on ${date.toLocaleString(DateTime.DATE_HUGE)}`;
    }

    return result;
  }
}
