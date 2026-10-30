/** @type {import('tailwindcss').Config} */
const token = (name) => `rgb(var(--${name}) / <alpha-value>)`;

export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        canvas: token("canvas"),
        surface: token("surface"),
        fill: token("fill"),
        label: token("label"),
        secondary: token("secondary"),
        tertiary: token("tertiary"),
        separator: token("separator"),
        accent: token("accent"),
        positive: token("positive"),
        negative: token("negative"),
        caution: token("caution")
      },
      fontFamily: {
        sans: [
          "-apple-system",
          "BlinkMacSystemFont",
          "SF Pro Text",
          "SF Pro Display",
          "Inter",
          "Segoe UI",
          "Roboto",
          "Helvetica Neue",
          "sans-serif"
        ],
        mono: ["SF Mono", "ui-monospace", "Menlo", "Consolas", "monospace"]
      },
      fontSize: {
        "large-title": ["2.125rem", { lineHeight: "2.5625rem", letterSpacing: "-0.022em" }],
        title: ["1.375rem", { lineHeight: "1.75rem", letterSpacing: "-0.016em" }],
        headline: ["1.0625rem", { lineHeight: "1.375rem", letterSpacing: "-0.01em" }],
        body: ["0.9375rem", { lineHeight: "1.375rem", letterSpacing: "-0.005em" }],
        footnote: ["0.8125rem", { lineHeight: "1.125rem" }]
      },
      borderRadius: {
        card: "1.125rem",
        control: "0.625rem"
      },
      boxShadow: {
        card: "var(--shadow-card)"
      },
      opacity: {
        12: "0.12"
      },
      transitionTimingFunction: {
        apple: "cubic-bezier(0.22, 1, 0.36, 1)",
        "apple-in": "cubic-bezier(0.4, 0, 1, 1)"
      },
      transitionDuration: {
        fast: "150ms",
        base: "250ms",
        slow: "350ms"
      }
    }
  },
  plugins: []
};
