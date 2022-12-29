export interface DialogResult<T> {
  cancelled: boolean;
  result?: T;
}
