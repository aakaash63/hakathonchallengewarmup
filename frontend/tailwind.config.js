/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        hope: {
          50: '#f0f9f4', 100: '#dcf0e6', 200: '#bbdecf', 300: '#8dc4ae',
          400: '#5da38c', 500: '#3d8870', 600: '#2e6e5a', 700: '#265849',
          800: '#214739', 900: '#1c3c30',
        },
        calm: {
          50: '#f0f4ff', 100: '#e0eaff', 200: '#c7d7fe', 300: '#a5bbfc',
          400: '#8098f9', 500: '#6172f3', 600: '#444ce7', 700: '#3538cd',
          800: '#2d31a6', 900: '#2d3282',
        },
      },
    },
  },
  plugins: [],
};
