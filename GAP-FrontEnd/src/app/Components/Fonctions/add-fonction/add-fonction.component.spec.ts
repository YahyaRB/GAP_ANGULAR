import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddFonctionComponent } from './add-fonction.component';

describe('AddFonctionComponent', () => {
  let component: AddFonctionComponent;
  let fixture: ComponentFixture<AddFonctionComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddFonctionComponent]
    });
    fixture = TestBed.createComponent(AddFonctionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
