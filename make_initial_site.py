import argparse
from pathlib import Path
import xml.etree.ElementTree as ET
import shutil
import datetime
import re

TITLE = 'Constellation'

ITEMS = '__items__'

INDEX_MB = '''<!--
Constellation documentation master file, created on {}.
-->
# {}

---

{}
'''


# TODO: put the unique key/id not on the MD page but rather in an index file?


def helpsets(dir):
    """Yield NetBeans HelpSet marker files."""

    for pinfo in dir.rglob('package-info.java'):
        with pinfo.open() as f:
            for line in f.readlines():
                if line.startswith('@HelpSetRegistration'):
                    q1 = line.index('"')
                    q2 = line.index('"', q1+1)
                    name = line[q1+1:q2]
                    hs = pinfo.with_name(name)
                    yield hs


def get_module(indir, toc_file):
    """The NetBeans module containing the toc_file.

    Assuming indir is the root directory of the NetBeans project,
    the module is the top-most directory of the toc_file under indir.
    """

    return toc_file.relative_to(indir).parts[0]


def parse_helpset(hs):
    """Parse a -hs.xml helpset file."""

    hs_xml = ET.parse(str(hs))
    root = hs_xml.getroot()
    # print(root)

    refs = {}
    for child in root:
        if child.tag=='maps':
            mapref = child.find('mapref')
            location = mapref.attrib['location']
            # print(location)
            refs['location'] = location
        elif child.tag=='view':
            type = child.find('type').text
            data = child.find('data').text
            refs[type] = data

    return refs


def parse_map(hs, m):
    """Parse a -map.html helpset mapping file."""

    m = hs.with_name(m)
    m_xml = ET.parse(str(m))
    root = m_xml.getroot()

    maps = {}
    for child in root:
        assert child.tag=='mapID'
        target = child.attrib['target']
        url = child.attrib['url']
        maps[target] = hs.with_name(url)

    return maps


def parse_toc(hs, toc, module):
    """Parse a -toc.xml helpset table-of-contents file.

    Slightly trickier, because there are levels of <tocitem> tags.
    Each level has a 'text' attrib, but only the leaves have
    a 'target' attrib'.

    Just do it recursively.
    """

    # Leaf items are collected in a list.
    #

    def toc_level(tocs, root):
        for item in root.findall('tocitem'):
            text = item.attrib['text']
            if 'target' in item.attrib:
                # This is a leaf referencing a help target.
                #
                tocs[ITEMS].append((text, item.attrib['target']))
            else:
                if text not in tocs:
                    tocs[text] = {ITEMS:[]}
                toc_level(tocs[text], item)

                # If there are no leaves at this level, remove the empty list.
                #
                if not tocs[text][ITEMS]:
                    del tocs[text][ITEMS]

    tocs = {}

    toc = hs.with_name(toc)
    toc_xml = ET.parse(str(toc))
    root = toc_xml.getroot()
    toc_level(tocs, root)

    return tocs


def merge_tocs(toc_list):
    """Merge a list of tocs into a single toc.

    Each level of toc is a dict with two optional keys:
    * name - the name of the level, contains a dict of the next level
    * ITEMS - a list of (name,target) tuples.

    Recursive, obviously.
    """

    def merge_level(merged, level):
        for k,v in level.items():
            if k==ITEMS:
                if ITEMS not in merged:
                    merged[ITEMS] = []
                merged[ITEMS].extend(v)
            else:
                if k not in merged:
                    merged[k] = {}
                merge_level(merged[k], v)

    toc1 = {}
    for toc in toc_list:
        merge_level(toc1, toc)

    return toc1


def generate_pages(outdir, merged_tocs, merged_maps):
    """Generate documentation in a proper directory hierarchy.

    This means an index.md file at each level.
    """

    def simple_name(name):
        return ''.join(c if '0'<=c<='9' or 'a'<=c<='z' else '_' for c in name.lower())

    def ensure_dir(dir, category):
        d = dir / category
        if not d.is_dir():
            d.mkdir()

    def tree(category, toc, levels):
        level = '/'.join(levels)
        ensure_dir(outdir, level)
        if ITEMS in toc:
            for doc in toc[ITEMS]:
                help_id = doc[1]
                in_html = merged_maps[help_id]
                out_md = outdir / level / Path(in_html).with_suffix('.md').name
                yield level, category, in_html, out_md, help_id
        for sub_category in toc:
            cat = simple_name(sub_category)
            if sub_category!=ITEMS:
                sublevel = levels[:]
                sublevel.append(cat)

                # Yield the index of the next level down.
                # index files don't have matching HTML files or NetBeans helpIds.
                #
                sl = '/'.join(sublevel)
                yield level, category, None, outdir / sl / 'index.md', None

                # Recursively yield the next level down.
                #
                yield from tree(sub_category, toc[sub_category], sublevel)

    yield from tree(TITLE, merged_tocs, [])


def box(lines):
    width = max(len(line) for line in lines)
    s = []
    s.append('*'*(width+4))
    for line in lines:
        s.append(f'* {line:<{width}} *')
    s.append('*'*(width+4))

    return '\n'.join(s)


if __name__=='__main__':

    def dir_req(s):
        """Require this parameter to be a directory, and convert it to a Path instance."""

        p = Path(s)
        if not p.is_dir():
            raise argparse.ArgumentTypeError('Must be a directory')

        return p

    parser = argparse.ArgumentParser(description='Process existing HTML to ReST.')
    parser.add_argument('--indir', type=dir_req, required=True, help='Directory containing NetBeans help')
    parser.add_argument('--outdir', type=dir_req, required=True, help='Output directory tree')
    parser.add_argument('--index', action='store_true', help='Add index.md files')

    args = parser.parse_args()
    # print(args)

    merged_maps = {}
    toc_list = []
    for hs in helpsets(args.indir):
        #print(hs)
        module = get_module(args.indir, hs)
        #print(module)

        refs = parse_helpset(hs)
        #print(refs)

        maps = parse_map(hs, refs['location'])
        #print(maps)

        for target, url in maps.items():
            if target in merged_maps:
                raise ValueError(f'Target {target} already found')
            merged_maps[target] = url

        toc = parse_toc(hs, refs['javax.help.TOCView'], module)
        #print(toc)
        toc_list.append(toc)

    merged_tocs = merge_tocs(toc_list)
    #print(merged_tocs.keys())
    #print(merged_maps)

    # We need an index.md in each directory.
    # Keep track of the levels so we can generate them at the end.
    #
    levels = {}

    resource_folders = set()

    regex_resources = re.compile(r"docs/.*", re.IGNORECASE)
    regex_last_directory = re.compile(r"[^\/]+\/?$", re.IGNORECASE)

    for level, category, in_html, out_md, help_id in generate_pages(args.outdir, merged_tocs, merged_maps):
        input = str(in_html).replace('.html', '.md')
        lc = level, category
        if lc not in levels:
            levels[lc] = []
        levels[lc].append(out_md)
        # print(levels)


        if in_html:
            # This is a help .rst file (not a category / index.rst file).
            #
            #print(f'copy {input} -> {out_md}')
            shutil.copy(input, out_md)

            resource = regex_resources.sub('docs/resources/', str(in_html))
            resource_folders.add(resource)

            print(out_md, regex_last_directory.sub('', str(out_md)))

    print(resource_folders)

    if args.index:
        # Create an index.md at each level.
        # Each index.md must have a reference to the index files below it.
        #

        print('\nGenerating toctree files...')

        now = datetime.datetime.now().isoformat(' ')[:19]
        for (level, category), md_files in levels.items():
            # create the nav for mkdocs.yml
            padding = '    '
            padding += '    ' * level.count('/')
            relative_path = str(args.outdir).replace('docs/', '') + '/' + level + '/' + 'index.md'

            # TODO: write this directly to the file
            # print(f'{padding}- \'{category}\':')
            # print(f'{padding}    - Overview: {relative_path}')

            # add the index files
            pages = []
            for page in md_files:
                p = Path(page)
                if p.name == 'index.md':
                    entry = f'{p.parent.name}/index'
                else:
                    entry = p.stem
                pages.append(f' - [{entry}]({entry}.md)')

            contents = INDEX_MB.format(now, category, '\n'.join(pages))
            index_path = args.outdir / level / 'index.md'
            with open(index_path, 'w') as f:
                f.write(contents)
    else:
        print()
        print(box([
            'No index.md files have been created.',
            'If this is Core, you probably want index.md files.',
            'If so, use --index.'
        ]))
