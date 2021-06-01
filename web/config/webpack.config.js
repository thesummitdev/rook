const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
  entry : {
    web : [ './main.ts', './styles/styles.scss' ],
  },
  output : {
    path : path.resolve(__dirname, '../build'),
    filename : 'bundle.js',
    clean : true,
  },

  mode : 'development',
  module : {
    rules : [
      {
        test : /\.tsx?$/,
        use : 'ts-loader',
        exclude : /node_modules/,
      },
      {
        test : /\.(sa|sc|c)ss$/,
        use : [
          {
            // After all CSS loaders we use plugin to do this work.
            // It gets all transformed CSS and extracts it into separate
            // single bundled file
            loader : MiniCssExtractPlugin.loader,
          },
          {
            // This loader resolves url() and @imports inside CSS
            loader : 'css-loader',
          },
          {
            // Then we apply post CSS fixes like autoprefixer and minifying
            loader : 'postcss-loader',
          },
          {
            loader : 'sass-loader',
            options : {
              implementation : require('sass'),
            },
          },
        ],
      },
    ],
  },
  resolve : {
    extensions : [ '.tsx', '.ts', '.js' ],
  },
  plugins : [
    new MiniCssExtractPlugin({
      filename : 'bundle.css',
    }),
    new HtmlWebpackPlugin({
      title : 'f.link',
    }),
  ],
};
