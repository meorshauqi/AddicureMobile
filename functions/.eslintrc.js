module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    "max-len": ["error", { "code": 100 }],
    "quotes": ["error", "double"],
    "comma-dangle": ["error", "always-multiline"],
    "indent": ["error", 2],
    "object-curly-spacing": ["error", "always"],
  },
};
