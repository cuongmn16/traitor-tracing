export const environment = {
  production: false,
  apiUrl: '/api',
  tracingUrl: '/api/tracing',
};

export function mediaUrl(filePath: string): string {
  if (!filePath) return '';
  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    return filePath;
  }
  return filePath.startsWith('/') ? filePath : `/${filePath}`;
}
