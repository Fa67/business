/**
 * Created by ZHL on 2016/3/31.
 */
'use strict';
var gulp = require('gulp'),
    usemin = require('gulp-usemin'),
    minifyCss = require('gulp-minify-css'),
    uglify = require('gulp-uglify'),
    rev = require('gulp-rev'),
    del = require('del'),
    annotate = require('gulp-ng-annotate'),
    sourceMaps = require('gulp-sourcemaps'),
    runSequence = require('run-sequence');

gulp.task('usemin', function () {
    return gulp.src(['index.html'])
        .pipe(usemin({
            appjs: [annotate(), rev()]
        }))
        .pipe(gulp.dest('app/'));
});

gulp.task('copy', function () {
    gulp.src('views/**/*.*')
        .pipe(gulp.dest('app/views'));

    gulp.src('dist/**/*.*')
        .pipe(gulp.dest('app/dist'));

    gulp.src('styles/**/*.*')
        .pipe(gulp.dest('app/styles'));

    gulp.src(['404.html', '.htaccess', 'robots.txt'])
        .pipe(gulp.dest('app/'));

    gulp.src('images/**/*.*')
        .pipe(gulp.dest('app/images'));

    gulp.src('favicon.ico')
        .pipe(gulp.dest('app/'));

    gulp.src('bower_components/**/*.*')
        .pipe(gulp.dest('app/bower_components'));

    gulp.src('META-INF/**/*.*')
        .pipe(gulp.dest('app/META-INF'));

    gulp.src('WEB-INF/**/*.*')
        .pipe(gulp.dest('app/WEB-INF'));
});

gulp.task('clean', function (done) {
    return del(['app/', '.tmp/'], done);
});

gulp.task('build', function (callback) {
    runSequence('clean', ['usemin', 'copy'], callback);
});
