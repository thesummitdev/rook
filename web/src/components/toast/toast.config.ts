import { Semantic } from 'web/src/util/enums';

/* The configuration for a Toast message. */
export interface ToastConfig {
  message: string;
  semantic: Semantic;
  duration: number;
}
