#!/usr/bin/python

import argparse
from pathlib import Path

HELP_MAP = 'help_map.txt'
DIRECTIVE = '.. help-id:'

# Make the "helpId to documentation page" mapping required by the NetBeans
# help system. This assumes that each .rst file that is the target of
# a helpId contains a
#
# .. help-id: this.is.a.unique.help.id
#
# directive. This is interpreted as a comment by ReStructuredText, but is
# easily found by a script like this one.
#

def extract_help_id(p):
    """If this file contains a help-id comment directive, extract the help-id value and return it."""

    with open(p) as f:
        for line in f:
            if line.startswith(DIRECTIVE):
                line = line.split(':')
                return line[1].strip()

    return None

if __name__=='__main__':

    def dir_req(s):
        """Require this parameter to be a directory, and convert it to a Path instance."""

        p = Path(s)
        if not p.is_dir():
            raise argparse.ArgumentTypeError('Must be a directory')

        return p

    def help_map_req(s):
        """Require this parameter to be a file."""

        p = Path(s)
        if p.is_dir():
            raise argparse.ArgumentTypeError('Must be a file')

        return p

    parser = argparse.ArgumentParser(description='Make a help_map.txt file.')
    parser.add_argument('--indir', type=dir_req, required=True, help='Directory containing .rst files')
    parser.add_argument('--out', type=help_map_req, required=True, help='The help_map.txt file')

    args = parser.parse_args()

    # Find all the .rst files.
    #
    mappings = []
    nfiles = 0
    for p in args.indir.rglob('*.rst'):
        nfiles += 1
        help_id = extract_help_id(p)
        if help_id:
            mappings.append((help_id, p.relative_to(args.indir)))

    # Write the help_map file.
    #
    with open(args.out, 'w') as f:
        for mapping in mappings:
            # Make sure the relative path has '/' instead of '\' (WindowsPath).
            #
            help_id = mapping[0]
            relative_rst = str(mapping[1].with_suffix('')).replace('\\', '/')
            print(f'{help_id},{relative_rst}.html', file=f)

    print(f'.rst files found: {nfiles}')
    print(f'help-ids found  : {len(mappings)}')
    print(f'help_map.txt    : {args.out}')
