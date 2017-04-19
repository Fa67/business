/**
 * Created by ZHL on 2016/3/15.
 */
'use strict';
var gulp = require('gulp'),
    inject = require('gulp-inject'),
    runSequence = require('run-sequence'),
    filesort = require('gulp-angular-filesort');

gulp.task('inject:js', function () {
    return gulp.src('app/index.html')
        .pipe(inject(
            gulp.src('app/**/*.js').pipe(filesort()), {relative: true}))
        .pipe(gulp.dest('app'));
});

gulp.task('inject:css', function () {
    return gulp.src('app/index.html')
        .pipe(inject(gulp.src('app/**/*.css', {read: false}), {relative: true}))
        .pipe(gulp.dest('app'));
});

gulp.task('inject',function (callback) {
    runSequence('inject:css', 'inject:js', callback);
});