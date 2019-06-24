.PHONY: build
build: README.html

.PHONY: clean
clean:
	rm -f README.html

README.html: BLOG.md
	pandoc -o $@ $<