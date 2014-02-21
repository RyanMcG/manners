SEPARATOR=============================================================================
ECHO=@/usr/bin/env echo -e
DOCS_BRANCH=gh-pages

# Generate documentation
doc:
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Create documentation..."
	$(ECHO) ${SEPARATOR}
	lein doc

# Copy generated docs into gh-pages branch
prepare_docs: doc
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Preparing docs in gh-pages branch. . ."
	$(ECHO) ${SEPARATOR}
	rm -rf .git/_deploy
	cp -R doc .git/_deploy
	git stash -q
	git checkout gh-pages
	cp -R .git/_deploy/* .
	git add .
	-git commit -m "Update documentation."
	@git checkout --force - > /dev/null && git stash pop -q
	$(ECHO)

# Deploy prepared documents
deploy_docs:
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Attempting deployment to origin's ${DOCS_BRANCH} branch."
	$(ECHO) ${SEPARATOR}
	git push -u origin ${DOCS_BRANCH}:${DOCS_BRANCH}

prepare_and_deploy: prepare_docs deploy_docs

# Should only be run once to make the gh-pages branch.
init_docs:
	$(ECHO)
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Initializing orphan ${DOCS_BRANCH} branch. . ."
	$(ECHO) ${SEPARATOR}
	git checkout --orphan ${DOCS_BRANCH}
	g rm -rf .
	rm -rf doc target Makefile
	touch index.html
	git add index.html
	$(ECHO)
	$(ECHO) "\tAttempting an initial commit. . ."
	$(ECHO)
	git commit -m "Initial commit."
