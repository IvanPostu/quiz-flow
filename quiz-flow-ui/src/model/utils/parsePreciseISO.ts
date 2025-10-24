import { parseISO } from "date-fns";

export function parsePreciseISO(isoString: string): Date {
  const normalized = isoString.replace(/\.(\d{3})\d*Z$/, ".$1Z");
  return parseISO(normalized);
}
