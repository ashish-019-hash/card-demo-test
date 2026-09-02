export type RequiredField = { label: string; value: string };

export function firstRequiredError(fields: RequiredField[]): string | null {
  const failed = fields.find((field) => field.value.trim() === '');
  return failed ? `${failed.label} is required.` : null;
}

export function normalizeUserId(value: string): string {
  return value.toUpperCase();
}
