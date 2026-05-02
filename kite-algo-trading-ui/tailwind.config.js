/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ember: "#b45309",
        tide: "#0f766e",
        ink: "#182321",
        mist: "#f5efe4"
      },
      fontFamily: {
        display: ["Avenir Next", "Segoe UI", "sans-serif"],
        body: ["Avenir Next", "Segoe UI", "sans-serif"]
      },
      boxShadow: {
        cloud: "0 24px 70px rgba(36, 34, 25, 0.12)"
      }
    }
  },
  plugins: []
};
