import { describe, expect, it } from 'vitest';
import { firstRequiredError, normalizeUserId } from '../lib/validation';

describe('frontend validation', () => {
  it('returns the first required field in source order', () => {
    expect(firstRequiredError([
      { label: 'First name', value: '' },
      { label: 'Last name', value: '' },
    ])).toBe('First name is required.');
  });

  it('normalizes user IDs to uppercase', () => {
    expect(normalizeUserId('admin001')).toBe('ADMIN001');
  });
});
