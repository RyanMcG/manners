SEPARATOR=============================================================================
ECHO=@/usr/bin/env echo -e
DOCS_BRANCH=gh-pages

# Generate documentation using marginalia
marg:
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Using marginalia to create documentation. . ."
	$(ECHO) ${SEPARATOR}
	@-mkdir -p docs/
	lein marg -d docs/ -f index.html

# Copy generated docs into gh-pages branch
prepare_docs: marg
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Preparing marginalia docs in gh-pages branch. . ."
	$(ECHO) ${SEPARATOR}
	@-mkdir -p .git/_deploy/
	rm -rf .git/_deploy/*
	cp docs/* .git/_deploy/
	git checkout gh-pages
	cp .git/_deploy/* .
	-git commit -am "Update documentation."
	@git checkout - > /dev/null
	$(ECHO)

# Deploy prepared documents
deploy_docs: prepare_docs
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Attempting deployment to origin's ${DOCS_BRANCH} branch."
	$(ECHO) ${SEPARATOR}
	git push -u origin ${DOCS_BRANCH}:${DOCS_BRANCH}

# Should only be run once to make the gh-pages branch.
init_docs:
	$(ECHO)
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Initializing orphan ${DOCS_BRANCH} branch. . ."
	$(ECHO) ${SEPARATOR}
	git checkout --orphan ${DOCS_BRANCH}
	g rm -rf .
	rm -rf docs target Makefile
	touch index.html
	git add index.html
	$(ECHO)
	$(ECHO) "\tAttempting an initial commit. . ."
	$(ECHO)
	git commit -m "Initial commit."
