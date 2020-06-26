# Pull Request Template

Following checklist help me to incorporate your contribution quickly:

- [ ] Each commit in the pull request should have a meaningful subject
      line and body.
- [ ] Format the pull request title like `[TAG] - Fixes bug in foo`,
      where you replace `TAG` with the appropriate GitHub issue.
      Best practice is to use the GitHub issue title in the pull request
      title and in the first line of the commit message.
- [ ] Write a pull request description that is detailed enough to understand
      what the pull request does, how, and why.
- [ ] Run `./mvnw clean verify` to make sure basic checks pass.
      A more thorough check will be performed on your pull request
      automatically.
- [ ] Run `./mvnw -Prun-its clean verify` to make sure integration tests pass.

## License

Your contribution should be under the [MIT License (MIT)][license] and
you have to acknowledge this by using the following checkbox:

- [ ] I hereby declare this contribution
      to be licenced under the [MIT License (MIT)][license]

[license]: https://spdx.org/licenses/MIT.html
