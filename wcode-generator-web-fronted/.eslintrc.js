module.exports = {
  extends: [require.resolve('@umijs/lint/dist/config/eslint')],
  globals: {
    page: true,
    REACT_APP_ENV: true,
  },
  rules: {
    '@typescript-eslint/no-unused-vars': [
      'warn', // 或 'error'
      {
        argsIgnorePattern: '^_', // 忽略以 "_" 开头的变量
        varsIgnorePattern: '^_', // 忽略以 "_" 开头的变量
      },
    ],
  },
};
